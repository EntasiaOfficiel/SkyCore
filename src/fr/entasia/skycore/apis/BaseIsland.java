package fr.entasia.skycore.apis;


import fr.entasia.apis.ChatComponent;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.otherobjs.CodePasser;
import fr.entasia.skycore.others.enums.Dimension;
import fr.entasia.skycore.others.enums.IslandType;
import fr.entasia.skycore.others.enums.MemberRank;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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


	protected long cooldown=10000;



	// CONSTRUCTEURS

	public BaseIsland(ISID isid, IslandType type){
		this.isid = isid;
		this.type = type;
		home = new Location(Dimension.OVERWORLD.world, isid.getMiddleX(), 70.2, isid.getMiddleZ());

		Dimension[] v = Dimension.values();

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

	public void teleportOverWord(Player p) {
		Dimension dim = Dimension.getDimension(p.getWorld());
		if(dim==Dimension.NETHER) {
			if(OWNetherPortal==null||OWNetherPortal.getBlock().getType()!= Material.PORTAL){
				DimensionHelper.findNetherPortal(this, p, Dimension.OVERWORLD);
			}else p.teleport(OWNetherPortal);
		}else if(dim==Dimension.END){
			if(OWEndPortal==null||
					(OWEndPortal.getBlock().getType()!= Material.ENDER_PORTAL&&OWEndPortal.getBlock().getType()!= Material.END_GATEWAY)){
					DimensionHelper.findEndPortal(this, p, Dimension.OVERWORLD);
			}else p.teleport(OWEndPortal);
		}
	}

	public void teleportNether(Player p) {
		Dimension dim = Dimension.getDimension(p.getWorld());
		if(hasNether){
			if(netherPortal==null||netherPortal.getBlock().getType()!= Material.PORTAL) {
				DimensionHelper.findNetherPortal(this, p, Dimension.NETHER);
			} else
				p.teleport(netherPortal);
		}
	}

	public void teleportEnd(Player p) {
		Dimension dim = Dimension.getDimension(p.getWorld());
		if (hasEnd) {
			if (endPortal == null ||
					(endPortal.getBlock().getType() != Material.ENDER_PORTAL && endPortal.getBlock().getType() != Material.END_GATEWAY)) {
				DimensionHelper.findEndPortal(this, p, Dimension.END);
			} else p.teleport(endPortal);
		}

	}

	// FONCTIONS A AVOIR


	public boolean equals(BaseIsland is){
		return is.isid.equals(isid);
	}

	public boolean equals(ISID isid){
		return this.isid.equals(isid);
	}

	public String toString(){
		return "BaseIsland["+isid+"]";
	}

	public int hashCode(){
		return isid.hashCode();
	}


	// FONCTIONS RANDOM

	private static final int time = 5*60*1000;

	public int updateLvl(CodePasser.Void code){
		long a = System.currentTimeMillis() - cooldown;
		if(a < time)return (int) (time-a)/1000;
		else {
			cooldown = System.currentTimeMillis();
			TerrainManager.calcPoints(this, code);
			return 0;
		}
	}



	public void allowNether(){
		hasNether = true;
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET hasNether=1 WHERE x=? and z=?", isid.x, isid.z);
	}

	public void allowEnd(){
		hasEnd = true;
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET hasEnd=1 WHERE x=? and z=?", isid.x, isid.z);
	}

	public boolean hasDimension(Dimension dim){
		if(dim==Dimension.OVERWORLD)return true;
		else if(dim==Dimension.NETHER)return hasNether;
		else if(dim==Dimension.END)return hasEnd;
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
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET malus=? WHERE x=? and z=?", malus, isid.x, isid.z);
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
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET name=? WHERE x=? and z=?", name, isid.x, isid.z);
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
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET home_w=? and home_x=? and home_y=? and home_z=? where x =? and z=?", Dimension.getDimension(home.getWorld()).id, isid.x, isid.z);
	}


	public ArrayList<ISPLink> getMembers(){
		return new ArrayList<>(members);
	}

	private static Comparator<ISPLink> memberComparator = Comparator.comparingInt(o -> o.getRank().id);

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
		if(rank==MemberRank.DEFAULT)InternalAPI.warn("Utilise removeMember() pour supprimer un joueur de l'île !");
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
				if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("INSERT INTO sky_pis (rank, x, z, uuid) VALUES (?, ?, ?, ?)", rank.id, isid.x, isid.z, sp.uuid);
				return true;
			}else InternalAPI.warn("Le joueur est déja sur l'île !");
		}
		return false;
	}

	public boolean removeMember(ISPLink link){
		if(link.is.equals(this)){
			link.setRank(MemberRank.DEFAULT);
			members.remove(link);
			link.sp.islands.remove(link);
			if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("DELETE FROM sky_pis WHERE x=? and z=? and uuid=?", isid.x, isid.z, link.sp.uuid);
			return true;
		} else InternalAPI.warn("L'île fournie ne correspond pas");
		return false;
	}

	public boolean reRankMember(ISPLink link, MemberRank rank){
		if(rank==MemberRank.DEFAULT) InternalAPI.warn("Utilise removeMember() pour supprimer un joueur de l'île !");
		else if(link.is.equals(this)){
			link.setRank(rank);
			if(rank==MemberRank.CHEF) {
				owner.setRank(MemberRank.ADJOINT);
				owner = link;
			}
			if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_pis SET rank = ? WHERE uuid=? and x=? and z=?", rank.id, link.sp.uuid, link.is.isid.x, link.is.isid.z);
			return true;
		} else InternalAPI.warn("L'île fournie ne correspond pas");
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
			if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("INSERT INTO sky_pis (rank, x, z, uuid) VALUES (?, ?, ?, ?)", 0, isid.x, isid.z, sp.uuid);
		}else{
			removeMember(link);
			if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_pis SET rank=? where x=? and z=? and uuid=?)", 0, isid.x, isid.z, sp.uuid);
		}
	}

	public void removeBanned(SkyPlayer sp){
		this.banneds.remove(sp);
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("DELETE FROM sky_pis WHERE x=? and z=? and uuid=?", isid.x, isid.z, sp.uuid);
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
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET bank+=? WHERE x=?, and z=?", m, isid.x, isid.z);
	}

	public void withdrawBank(long m){
		bank-=m;
		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET bank-=? WHERE x=?, and z=?", m, isid.x, isid.z);
	}

	// 400 - 799
}
