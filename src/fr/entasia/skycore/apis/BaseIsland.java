package fr.entasia.skycore.apis;


import com.mysql.fabric.xmlrpc.base.Array;
import fr.entasia.apis.ChatComponent;
import fr.entasia.apis.Serialization;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.objs.AutoMiner;
import fr.entasia.skycore.objs.CodePasser;
import fr.entasia.skycore.others.enums.Dimensions;
import fr.entasia.skycore.others.enums.IslandType;
import fr.entasia.skycore.others.enums.MemberRank;
import fr.entasia.skycore.others.tasks.AutoMinerTask;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class BaseIsland {

	public final ISID isid;
	public final IslandType type;

	protected String name=null;
	protected Location home;
	protected ISPLink owner;
	protected ArrayList<ISPLink> members = new ArrayList<>();
	protected ArrayList<SkyPlayer> banneds = new ArrayList<>();
	protected ArrayList<SkyPlayer> coops = new ArrayList<>();
	protected ArrayList<SkyPlayer> invites = new ArrayList<>();

	protected long bank=0;
	protected byte extension; // 0-3

	protected long rawpoints=0;
	protected int malus=0;
	protected long lvl=0;
	protected int remPoints=1;

	protected boolean hasNether, hasEnd;
	protected Location netherPortal, endPortal, OWNetherPortal, OWEndPortal;


	// online stuff
	public boolean loaded = false;
	protected long lvlCooldown =10000;
	public ArrayList<AutoMiner> autominers = new ArrayList<>();
	public boolean generating = false;



	// CONSTRUCTEURS

	public BaseIsland(ISID isid, IslandType type){
		this.isid = isid;
		this.type = type;
		home = new Location(Dimensions.OVERWORLD.world, isid.getMiddleX(), 70.2, isid.getMiddleZ());

		Dimensions[] v = Dimensions.values();

	}

	// FONCTIONS UTILES

	public int getMinXBuild(){
		return isid.getMinXTotal()+(3-extension)*50;
	}
	public int getMinZBuild(){
		return isid.getMinZTotal()+(3-extension)*50;
	}
	public int getMaxXBuild(){
		return isid.getMaxXTotal()-(3-extension)*50;
	}
	public int getMaxZBuild(){
		return isid.getMaxZTotal()-(3-extension)*50;
	}


	/*
	Instructions :
	- regarder si la dimension à été unlock
	- regarder si le point de tp existe
	- s'il existe pas on le re set
	- on tp
	- PRIER BORDEL
	 */

	// A FAIRE : FAIRE RETURN LA LOC (donc passer en thread child)


	public Location getOWPortal(Dimensions from){
		if(from==Dimensions.NETHER)return OWNetherPortal;
		else if(from==Dimensions.END)return OWNetherPortal;
		else{
			InternalAPI.warn("Invalid dimension location request", true);
			return null;
		}
	}


	public void teleportOW(Dimensions from, Player p) {
		if(from==Dimensions.NETHER) {
			if(OWNetherPortal==null||OWNetherPortal.getBlock().getType()!= Material.PORTAL){
				PortalHelper.findNetherPortal(this, p, Dimensions.OVERWORLD);
			}else p.teleport(OWNetherPortal);
		}else if(from==Dimensions.END){
			if(OWEndPortal==null||
					(OWEndPortal.getBlock().getType()!= Material.ENDER_PORTAL&&OWEndPortal.getBlock().getType()!= Material.END_GATEWAY)){
					PortalHelper.findEndPortal(this, p, Dimensions.OVERWORLD);
			}else p.teleport(OWEndPortal);
		}
	}

	public boolean teleportNether(Player p) {
		if(hasNether){
			if(netherPortal==null||netherPortal.getBlock().getType()!= Material.PORTAL) {
				PortalHelper.findNetherPortal(this, p, Dimensions.NETHER);
			} else
				p.teleport(netherPortal);
		}else return false;
		return true;
	}

	public boolean teleportEnd(Player p) {
		if (hasEnd) {
			if (endPortal == null ||
					(endPortal.getBlock().getType() != Material.ENDER_PORTAL && endPortal.getBlock().getType() != Material.END_GATEWAY)) {
				PortalHelper.findEndPortal(this, p, Dimensions.END);
			} else p.teleport(endPortal);
		}else return false;
		return true;
	}

	// FONCTIONS A AVOIR


	public boolean equals(BaseIsland is){
		return is.isid.equals(isid);
	}

	public boolean equals(ISID isid){
		return this.isid.equals(isid);
	}

	public String toString(){
		return "BaseIsland["+isid.str()+"]";
	}

	public int hashCode(){
		return isid.hashCode();
	}


	// FONCTIONS RANDOM

	private static final int time = 5*60*1000;

	public int updateLvl(CodePasser.Void code){
		long a = System.currentTimeMillis() - lvlCooldown;
		if(a < time)return (int) (time-a)/1000;
		else {
			lvlCooldown = System.currentTimeMillis();
			TerrainManager.calcPoints(this, code);
			return 0;
		}
	}

	public boolean hasDimension(Dimensions dim){
		if(dim==Dimensions.OVERWORLD)return true;
		else if(dim==Dimensions.NETHER)return hasNether;
		else if(dim==Dimensions.END)return hasEnd;
		else return false;
	}



	public long getLevel(){
		return lvl;
	}

	public int getRemPoints(){
		return remPoints;
	}

	public void setMalus(int malus){
		this.malus = malus;
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET malus=? WHERE x=? and z=?", malus, isid.x, isid.z);
	}

	public int getMalus(){
		return malus;
	}



	public String getNameOrID(){
		if(name==null)return isid.str();
		else return name;
	}
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET name=? WHERE x=? and z=?", name, isid.x, isid.z);
	}



	public Location getHome(){
		return home;
	}

	public void teleportHome(Player p){
		p.teleport(home);
		p.setFallDistance(0);
	}

	public void setHome(Location home){
		this.home = home;
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET home_w=? and home_x=? and home_y=? and home_z=? where x =? and z=?", Dimensions.getDimension(home.getWorld()).id, isid.x, isid.z);
	}



	public ArrayList<ISPLink> getMembers(){
		return new ArrayList<>(members);
	}

	private static final Comparator<ISPLink> memberComparator = Comparator.comparingInt(o -> o.getRank().id);

	public ArrayList<ISPLink> getSortedMembers(){
		ArrayList<ISPLink> a = getMembers();
		a.sort(memberComparator);
		Collections.reverse(a);
		return a;
	}

	public ISPLink getMember(UUID uuid){
		for(ISPLink link : members){
			if(link.getRank()!=MemberRank.DEFAULT&&link.sp.uuid.equals(uuid))return link;
		}
		return null;
	}

	public boolean addMember(SkyPlayer sp, MemberRank rank){
		if(rank==MemberRank.DEFAULT)InternalAPI.warn("Utilise removeMember() pour supprimer un joueur de l'île !", true);
		else{
			ISPLink link = getMember(sp.uuid);
			if(link==null){
				link = new ISPLink(this, sp, rank);
				if(rank==MemberRank.CHEF){
					if(owner!=null) owner.setRank(MemberRank.ADJOINT);
					owner = link;
				}
				members.add(link);
				sp.islands.add(link);
				if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("INSERT INTO sky_pis (rank, x, z, uuid) VALUES (?, ?, ?, ?)", rank.id, isid.x, isid.z, sp.uuid);
				return true;
			}else InternalAPI.warn("Le joueur est déja sur l'île !", true);
		}
		return false;
	}

	public boolean removeMember(ISPLink link){
		if(link.is.equals(this)){
			link.setRank(MemberRank.DEFAULT);
			members.remove(link);
			link.sp.islands.remove(link);
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("DELETE FROM sky_pis WHERE x=? and z=? and uuid=?", isid.x, isid.z, link.sp.uuid);
			return true;
		} else InternalAPI.warn("L'île fournie ne correspond pas", true);
		return false;
	}

	public boolean reRankMember(ISPLink link, MemberRank rank){
		if(rank==MemberRank.DEFAULT) InternalAPI.warn("Utilise removeMember() pour supprimer un joueur de l'île !", true);
		else if(link.is.equals(this)){
			link.setRank(rank);
			if(rank==MemberRank.CHEF) {
				owner.setRank(MemberRank.ADJOINT);
				owner = link;
			}
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_pis SET rank = ? WHERE uuid=? and x=? and z=?", rank.id, link.sp.uuid, link.is.isid.x, link.is.isid.z);
			return true;
		} else InternalAPI.warn("L'île fournie ne correspond pas", true);
		return false;
	}


	public ArrayList<SkyPlayer> getInvites(){
		return new ArrayList<>(invites);
	}

	public boolean invitePlayer(SkyPlayer sp){
		if(isInvited(sp))return false;
		else{
			invites.add(sp);
			sp.invites.add(this);
			if(sp.p!=null)sp.p.sendMessage();
			return true;
		}
	}

	public boolean isInvited(SkyPlayer sp){
		for(SkyPlayer lsp : invites){
			if(lsp.equals(sp))return true;
		}
		return false;
	}

	public boolean cancelInvite(SkyPlayer sp){
		sp.invites.remove(this);
		return invites.remove(sp);
	}


	private static final BaseComponent[] b1 = ChatComponent.create("§3Is-Chat§b>> ");
	private static final BaseComponent[] b2 = ChatComponent.create(" §8| §7");

	public void islandChat(ISPLink link, String msg){
		islandChat(link, ChatComponent.create(msg));
	}

	public void islandChat(ISPLink link, BaseComponent... msg){
		sendTeamMsg(ChatComponent.create(b1, ChatComponent.create(link.getName()), b2, msg));
	}

	public void sendTeamMsg(String msg){
		sendTeamMsg(ChatComponent.create(msg));
	}

	public void sendTeamMsg(BaseComponent[] msg){
		for(ISPLink link : members){
			if(link.sp.p!=null){
				link.sp.p.sendMessage(msg);
			}
		}
	}


	public ISPLink getOwner(){
		return owner;
	}


	public ArrayList<SkyPlayer> getBanneds(){
		return new ArrayList<>(banneds);
	}

	public void addBanned(SkyPlayer sp){
		banneds.add(sp);
		ISPLink link = getMember(sp.uuid);
		if(link==null){
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("INSERT INTO sky_pis (rank, x, z, uuid) VALUES (?, ?, ?, ?)", 0, isid.x, isid.z, sp.uuid);
		}else{
			removeMember(link);
			if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_pis SET rank=? where x=? and z=? and uuid=?)", 0, isid.x, isid.z, sp.uuid);
		}
	}

	public void removeBanned(SkyPlayer sp){
		this.banneds.remove(sp);
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("DELETE FROM sky_pis WHERE x=? and z=? and uuid=?", isid.x, isid.z, sp.uuid);
	}

	public boolean isBanned(SkyPlayer sp){
		return this.banneds.contains(sp);
	}


	public byte getExtension(){
		return extension;
	}

	public void setExtension(byte extension){
		this.extension = extension;
	}


	public long getBank(){
		return bank;
	}

	public void addBank(long m){
		bank+=m;
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET bank+=? WHERE x=?, and z=?", m, isid.x, isid.z);
	}

	public void withdrawBank(long m){
		bank-=m;
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET bank-=? WHERE x=?, and z=?", m, isid.x, isid.z);
	}

	// 400 - 799



	public void tryLoad(){
		if(!loaded){
			loaded = true;
			try{
				ResultSet rs = Main.sqlite.fastSelectUnsafe("SELECT * FROM autominers WHERE is_x=? and is_z=? ", isid.x, isid.z);
				Block b;
				World w;
				Location loc;
				ItemStack item;
				while(rs.next()){
					item = Serialization.deserialiseItem(rs.getString("item"));
					w = Bukkit.getWorld(rs.getString("world"));
					if(w!=null){
						b = w.getBlockAt(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
						System.out.println(1);
						if(b.getType()!=Material.AIR){
							System.out.println(2);
							AutoMiner am = new AutoMiner(b, item);
							int i = 0;
							for(Entity ent : b.getLocation().getNearbyEntitiesByType(ArmorStand.class, 0.5)){
								if("AMPickaxe".equals(ent.getCustomName())){
									if(i==4){
										i = 5;
										break;
									}
									am.armorStands[i] = (ArmorStand) ent;
									i++;
								}
							}
							System.out.println("n="+i);
							if(i==4) {
							System.out.println(3);
								am.delete();
								am.spawn();
								autominers.add(am);
								AutoMinerTask.miners.add(am);
								continue;
							}
						}
						AutoMiner.deleteByBlock(b);
						w.dropItem(b.getLocation(), item);
					}
					Main.sqlite.fastUpdate("DELETE FROM autominers WHERE x=? and y=? and z=?", rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));

				}
			}catch(SQLException e){
				e.printStackTrace();
				Main.sqlite.broadcastError();
				InternalAPI.warn("Erreur lors du chargement des autominers de l'île "+isid.str()+" !", false);
			}
		}
	}


}
