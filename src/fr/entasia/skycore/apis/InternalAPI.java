package fr.entasia.skycore.apis;

import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.errors.EntasiaException;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.others.enums.Dimensions;
import fr.entasia.skycore.others.enums.IslandType;
import fr.entasia.skycore.others.enums.MemberRank;

import java.sql.ResultSet;
import java.util.UUID;

public class InternalAPI {

	public static byte postenable=0;
	public static boolean enableIGSQL=true;


	public static boolean SQLEnabled(){
		return InternalAPI.postenable==2&&InternalAPI.enableIGSQL&&Main.sql !=null;
	}
	public static boolean isFullyEnabled(){
		return InternalAPI.postenable==2;
	}

	public static void warn(String msg, boolean stack) {
		if(stack)new EntasiaException("Warning Skyblock").printStackTrace();
		Main.main.getLogger().warning(msg);
		ServerUtils.permMsg("logs.warn", "§6Warning Skyblock : §c"+msg);
	}



	public static void a(String a) {
		Main.main.getLogger().warning(a);


//		if(Main.enableDev){
//			Main.main.getLogger().warning(a);
//			Main.main.getLogger().warning("Une erreur à été rencontrée, mais le mode développement est actif");
//		}else throw new RuntimeException(a);
	}

	public static void onPostEnable(){ // besoin que les mondes soient chargés, voir BaseEvents
		try{
			if(postenable==0){
				postenable=1;
				Main.main.getLogger().info("Activation POST du plugin méga-badass");

				if(Main.sql!=null)loadIslands();

				postenable=2;
			}
		}catch(Throwable e){
			e.printStackTrace();
			if(!Main.enableDev){
				Main.main.getLogger().severe("Erreur lors du chargement POST du plugin ! ARRET DU SERVEUR");
				Main.main.getServer().shutdown();
			}
		}
	}

	public static void loadIslands() throws Throwable{
		long time = System.currentTimeMillis();


		ResultSet rs = Main.sql.connection.prepareStatement("SELECT * FROM sky_islands").executeQuery();
		BaseIsland is=null;
		while(rs.next()){
			is = new BaseIsland(new ISID(rs.getInt("x"), rs.getInt("z")), IslandType.getType(rs.getInt("type")));

			is.setName(rs.getString("name"));
			is.addBank(rs.getLong("bank"));
			is.setExtension(rs.getByte("extension"));

			if(rs.getByte("hasNether")==1)is.hasNether = true;
			if(rs.getByte("hasEnd")==1)is.hasEnd = true;
			Utils.islandCache.add(is);
		}

		rs = Main.sql.connection.prepareStatement("SELECT global.name, sky_players.* from sky_players INNER JOIN global ON sky_players.uuid = global.uuid").executeQuery();
		SkyPlayer sp=null;
		while(rs.next()){
			sp = new SkyPlayer(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
			sp.money = rs.getLong("money");
			sp.lastGenerated = rs.getInt("lastgen");
			Utils.playerCache.add(sp);
		}
		int i = 0;
		int rID;
		if(is==null) a("Aucune ile en mémoire !");
		else if(sp==null) a("Aucun joueur en mémoire !");
		else{
			rs = Main.sql.connection.prepareStatement("SELECT * FROM sky_pis").executeQuery();
			ISID isid;
			while(rs.next()){
				i++;
				isid = new ISID(rs.getInt("x"), rs.getInt("z"));

				if(!is.isid.equals(isid))is = BaseAPI.getIsland(isid);
				assert is != null;

				sp = BaseAPI.getSkyPlayer(UUID.fromString(rs.getString("uuid")));
				assert sp != null;


				rID = rs.getInt("rank");
				if(rID==0)is.addBanned(sp);
				else is.addMember(sp, MemberRank.getType(rID));
				if(rs.getByte("def")==1){
					if(sp.getDefaultIS()!=null){
						Main.main.getLogger().warning("Redéfinition de île par défaut pour "+sp.name+" !");
					}
					sp.setDefaultIS(isid);
				}

			}
		}
		Main.main.getLogger().info("Données chargées en "+(System.currentTimeMillis()-time)+"ms :");
		Main.main.getLogger().info(Utils.islandCache.size()+" iles");
		Main.main.getLogger().info(Utils.playerCache.size()+" joueurs");
		Main.main.getLogger().info(i+" liens");

	}



	public static boolean allowDimension(BaseIsland is, Dimensions d) {
		ServerUtils.wantChildThread();
		try{
			if(d==Dimensions.NETHER){
				TerrainManager.genNether(is.isid);
				is.hasNether = true;
				if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET hasNether=1 WHERE x=? and z=?", is.isid.x, is.isid.z);
			}else if(d==Dimensions.END){
				TerrainManager.genEnd(is.isid);
				is.hasEnd = true;
				if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET hasEnd=1 WHERE x=? and z=?", is.isid.x, is.isid.z);
			}else{
				InternalAPI.warn("Activation d'une dimension invalide : "+d, true);
			}
		}catch(Exception e){
			e.printStackTrace();
			InternalAPI.warn("Erreur d'activation de dimension "+d+" ! "+is.isid, false);
			return false;
		}
		return true;
	}

}
