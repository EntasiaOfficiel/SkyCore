package fr.entasia.skycore.apis;

import fr.entasia.apis.ServerUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.otherobjs.CodePasser;
import fr.entasia.skycore.others.enums.Dimension;
import fr.entasia.skycore.others.enums.MemberRank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static fr.entasia.skycore.Utils.playerCache;

public class BaseAPI {

	// BORDEL

	public static UUID parseArg(String str) {
		try{
			return UUID.fromString(str);
		}catch(IllegalArgumentException ignore){
			OfflinePlayer p = Bukkit.getPlayer(str);
			if(p==null){
				p = Bukkit.getOfflinePlayer(str);
				if(p==null)return null;
				else return p.getUniqueId();
			}else return p.getUniqueId();
		}
	}


	public static SkyPlayer getAutomatedSP(CommandSender p, String str) {
		UUID uuid = parseArg(str);
		if(uuid==null)p.sendMessage("§cCe joueur n'existe pas ou n'est pas inscrit en Skyblock !");
		else{
			SkyPlayer sp = getSkyPlayer(uuid);
			if(sp==null)p.sendMessage("§cCe joueur n'existe pas ou n'est pas inscrit en Skyblock !");
			else return sp;
		}
		return null;
	}

	// GET

	public static BaseIsland getIsland(ISID isid) {
		for(BaseIsland bis : Utils.islandCache){
			if(bis.isid.equals(isid))return bis;
		}
		return null;
	}

	public static SkyPlayer getOnlineSP(UUID uuid){
		for(SkyPlayer sp : Utils.onlineSPCache){
			if(sp.uuid.equals(uuid))return sp;
		}
		return null;
	}

	public static SkyPlayer getSkyPlayer(UUID uuid){
		for(SkyPlayer sp : playerCache){
			if(sp.uuid.equals(uuid))return sp;
		}
		return null;
	}

	public static SkyPlayer getSkyPlayer(OfflinePlayer p) {
		return getSkyPlayer(p.getUniqueId());
	}

	// FIRST SAVE - REGISTER

	@Deprecated
	public static SkyPlayer registerSkyPlayer(Player p) throws SQLException {
		SkyPlayer sp = new SkyPlayer(p);
		playerCache.add(sp);
		if(InternalAPI.SQLEnabled()){
			Main.sqlConnection.checkConnect();
			Main.sqlConnection.fastUpdateUnsafe("INSERT INTO sky_players (uuid) VALUES (?)", p.getUniqueId());

			// A DEL

			try {
				Main.sqlConnection.fastUpdateUnsafe("INSERT IGNORE INTO global (uuid, name) VALUES (?, ?)", p.getUniqueId().toString(), p.getName());
			}catch(SQLException ignore){

			}
		}

		return sp;
	}

	@Deprecated
	public static void registerIsland(BaseIsland is, SkyPlayer sp) throws SQLException {
		if(InternalAPI.SQLEnabled()){
			Main.sqlConnection.checkConnect();
			PreparedStatement ps = Main.sqlConnection.connection.prepareStatement("INSERT INTO sky_islands (x, z, type) VALUES (?, ?, ?)");
			ps.setInt(1, is.isid.x);
			ps.setInt(2, is.isid.z);
			ps.setInt(3, is.type.id);
			ps.execute();
		}
//		ps = Main.sqlConnection.connection.prepareStatement("INSERT INTO sky_pis (uuid, x, z, rank) VALUES (?, ?, ?, 5)");
//		ps.setString(1, sp.p.getUniqueId().toString());
//		ps.setInt(2, is.isid.x);
//		ps.setInt(3, is.isid.z);
//		ps.execute();

		is.addMember(sp, MemberRank.CHEF);

		Utils.islandCache.add(is);
	}
	// DELETE

	@Deprecated
	public static void deleteIsland(BaseIsland is, CodePasser.Bool code){
		ServerUtils.wantMainThread();
		if(InternalAPI.SQLEnabled()){
			if(Main.sqlConnection.fastUpdate("DELETE FROM sky_islands WHERE x=? AND z=?", is.isid.x, is.isid.z)==-1||
					Main.sqlConnection.fastUpdate("DELETE FROM sky_pis WHERE x=? AND z=?", is.isid.x, is.isid.z)==-1){
				code.run(true);
				return;
			}
		}

		int minx = is.getMinXBuild();
		int maxx = is.getMaxXBuild();
		int minz = is.getMinZBuild();
		int maxz = is.getMaxZBuild();

		for (ISPLink link : is.getMembers()) {
			link.setRank(MemberRank.DEFAULT);
			link.sp.islands.remove(link);
		}
		is.members.clear();

		Utils.islandCache.remove(is);

		for (Player p : Bukkit.getOnlinePlayers()) {
			Location loc = p.getLocation();
			if (loc.getX() > minx && loc.getZ() > minz && loc.getX() < maxx && loc.getZ() < maxz) {
				p.sendMessage("§cL'île sur laquelle tu étais viens d'être supprimée, tu as été téléporté au Spawn !");
				p.teleport(Utils.spawn);
			}
		}


		new BukkitRunnable() {
			@Override
			public void run() {
				if(TerrainManager.clearTerrain(is, TerrainManager.getSession(Dimension.OVERWORLD.world))&&TerrainManager.clearTerrain(is, TerrainManager.getSession(Dimension.NETHER.world))&&
						TerrainManager.clearTerrain(is, TerrainManager.getSession(Dimension.END.world))){
					code.run(false);
				}else code.run(true);
			}
		}.runTaskAsynchronously(Main.main);
	}

	@Deprecated
	public static boolean deleteSkyPlayer(SkyPlayer sp) {

		int a = Main.sqlConnection.fastUpdate("DELETE FROM sky_players WHERE uuid=?", sp.uuid);
		if(a==-1)return false;
		a = Main.sqlConnection.fastUpdate("DELETE FROM sky_pis WHERE uuid=?", sp.uuid.toString());
		if(a==-1)return false;

		playerCache.remove(sp);
		if(sp.isOnline())sp.p.kickPlayer("§cTon compte Skyblock à été supprimé. Merci de te reconnecter pour procéder à la regénération d'un compte");
		return true;
	}

}
