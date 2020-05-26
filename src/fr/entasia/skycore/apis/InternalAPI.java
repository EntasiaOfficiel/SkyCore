package fr.entasia.skycore.apis;

import fr.entasia.apis.ServerUtils;
import fr.entasia.errors.EntasiaException;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.others.enums.IslandType;
import fr.entasia.skycore.others.enums.MemberRank;

import java.sql.ResultSet;
import java.util.UUID;

public class InternalAPI {

	public static byte postenable=0;
	public static boolean enableIGSQL=true;



	public static boolean SQLEnabled(){
		return InternalAPI.postenable==2&&InternalAPI.enableIGSQL&&Main.sqlConnection!=null;
	}
	public static boolean isFullyEnabled(){
		return InternalAPI.postenable==2;
	}

	public static void warn(String msg) {
		new EntasiaException("Warning SkyTools").printStackTrace();
		Main.main.getLogger().warning(msg);
		ServerUtils.permMsg("logs.warn", "§6Warning SkyTools : §c"+msg);
	}



	public static void checkA(String a) {
		if(Main.enableDev){
			Main.main.getLogger().info(a);
			Main.main.getLogger().info("Une erreur à été rencontrée, mais le mode développement est actif");
		}else throw new RuntimeException(a);
	}


	public static void onPostEnable(){ // besoin que les mondes soient chargés, voir BaseEvents
		try{
			if(postenable==0){
				postenable=1;
				Main.main.getLogger().info("Activation POST du plugin méga-badass");

				if(Main.sqlConnection!=null)loadIslands();

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
		ResultSet rs = Main.sqlConnection.connection.prepareStatement("SELECT * FROM sky_islands").executeQuery();
		BaseIsland is=null;
		while(rs.next()){
			is = new BaseIsland(new ISID(rs.getInt("x"), rs.getInt("z")), IslandType.getType(rs.getInt("type")));

			is.setName(rs.getString("name"));
			is.addBank(rs.getLong("bank"));
			is.setExtension(rs.getByte("extension"));

//					if(rs.getByte("hasNether")==1)is.allowNether();
//					if(rs.getByte("hasEnd")==1)is.allowEnd();
			is.allowNether();
			is.allowEnd();
			Utils.islandCache.add(is);
		}

		rs = Main.sqlConnection.connection.prepareStatement("SELECT global.name, sky_players.* from sky_players INNER JOIN global ON sky_players.uuid = global.uuid").executeQuery();
		SkyPlayer sp=null;
		while(rs.next()){
			sp = new SkyPlayer(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
			sp.addMoney(rs.getLong("money"));
//					 sp.setDefaultIS(new ISID(rs.getInt("dis_x"), rs.getInt("dis_z")));
			Utils.playerCache.add(sp);
		}
		int i = 0;
		if(is==null)checkA("Aucune ile en mémoire !");
		else if(sp==null)checkA("Aucun joueur en mémoire !");
		else{
			rs = Main.sqlConnection.connection.prepareStatement("SELECT * FROM sky_pis").executeQuery();
			ISID isid;
			while(rs.next()){
				i++;
				isid = new ISID(rs.getInt("x"), rs.getInt("z"));

				if(!is.isid.equals(isid))is = BaseAPI.getIsland(isid);
				assert is != null;

				sp = BaseAPI.getSkyPlayer(UUID.fromString(rs.getString("uuid")));
				assert sp != null;


				is.addMember(sp, MemberRank.getType(rs.getInt("rank")));
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

}
