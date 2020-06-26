package fr.entasia.skycore;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import fr.entasia.apis.sql.SQLConnection;
import fr.entasia.skycore.apis.TerrainManager;
import fr.entasia.skycore.commands.*;
import fr.entasia.skycore.events.*;
import fr.entasia.skycore.objs.IslandShematics;
import fr.entasia.skycore.objs.islevel.BlockType;
import fr.entasia.skycore.others.enums.Dimensions;
import fr.entasia.skycore.others.enums.IslandType;
import fr.entasia.skycore.others.tasks.AutoMinerTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;

public class Main extends JavaPlugin {

	/*
	Stratégie actuelle :

	- Charger toutes les iles au démarrage
	- Charger tout les joueurs au démarragze
	 */

	public static Main main;
	public static boolean enableDev;

	public static SQLConnection sql;
	public static SQLConnection sqlite;

	public static File blockValuesFile;
	public static FileConfiguration blockValues;

	@Override
	public void onEnable(){
		try{
			main = this;
			getLogger().info("Activation du plugin méga-badass");
			Utils.spawnWorld = Bukkit.getWorlds().get(0);

			enableDev = main.getConfig().getBoolean("dev", false);

			loadConfigs();

			if(!enableDev){
				if(getConfig().getString("sqluser")!=null){
					sql = new SQLConnection(main.getConfig().getString("sqluser"), "playerdata");
				}
			}
			sqlite = new SQLConnection().sqlite("plugins/"+getName()+"/database.db");
			sqlite.unsafeConnect();

			getServer().getPluginManager().registerEvents(new BaseEvents(), this);
			getServer().getPluginManager().registerEvents(new IslandEvents(), this);
			getServer().getPluginManager().registerEvents(new IslandEvents2(), this);
			getServer().getPluginManager().registerEvents(new ChatEvents(), this);
			getServer().getPluginManager().registerEvents(new MiningEvents(), this);

			getCommand("skycore").setExecutor(new SkyCoreCommand());
			getCommand("spawn").setExecutor(new SpawnCommand());
			getCommand("bin").setExecutor(new BinCommand());
			getCommand("is").setExecutor(new IsCommand());
			getCommand("isadmin").setExecutor(new IsAdminCommand());
			getCommand("setspawn").setExecutor(new SetSpawnCommand());

			getCommand("baltop").setExecutor(new BaltopCommand());
			getCommand("money").setExecutor(new MoneyCommand());
			getCommand("pay").setExecutor(new PayCommand());
			getCommand("eco").setExecutor(new EcoCommand());

			loadIslandStructs();

			new AutoMinerTask().runTaskTimerAsynchronously(this, 0, 20*6); // full cycle

		}catch(Throwable e){
			e.printStackTrace();
			if(!enableDev){
				getLogger().severe("Erreur lors du chargement du plugin ! ARRET DU SERVEUR");
				getServer().shutdown();
			}
		}
	}

	public static class VoidGenerator extends ChunkGenerator {

		@Override
		public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
			return createChunkData(world);
		}
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new VoidGenerator();
	}

	public static void loadConfigs() throws Exception {
		main.saveDefaultConfig();
		blockValuesFile = new File(main.getDataFolder(), "blockvalues.yml");
		if(!blockValuesFile.exists()) Files.copy(main.getResource("blockvalues.yml"), blockValuesFile.toPath());

		blockValues = YamlConfiguration.loadConfiguration(blockValuesFile);
		main.reloadConfig();

		ConfigurationSection sec = main.getConfig().getConfigurationSection("spawn");
		Utils.spawn = new Location(Utils.spawnWorld, sec.getInt("x")+0.5, sec.getInt("y") + 0.2,
				sec.getInt("z")+0.5, sec.getInt("yaw"), sec.getInt("pitch"));

		Object o;
		Material m;
		BlockType bt;
		for(String k : blockValues.getKeys(false)){
			o = blockValues.get(k);
			m = Material.getMaterial(k);
			if(o instanceof ConfigurationSection){
				sec = (ConfigurationSection)o;
				bt = new BlockType();
				for(String k2 : sec.getKeys(false)){
					if(k2.equals("others")){
						bt.others = sec.getInt("others");
					}else{
						bt.prices.put(Integer.parseInt(k2), sec.getInt(k2));
					}
				}
			}else{
				TerrainManager.blockValues.put(m, new BlockType(Integer.parseInt(o.toString())));
			}
		}

	}

	public void loadIslandStructs() throws Exception {
		ArrayList<IslandShematics> allis = new ArrayList<>();
		String[] base = new String[]{"geante"};
		for(IslandType it : IslandType.values()){
			it.schems = new IslandShematics();
			it.schems.name = it.name().toLowerCase();
			it.schems.plans = base;
			allis.add(it.schems);
		}
		Dimensions.NETHER.schems = new IslandShematics();
		Dimensions.NETHER.schems.name = "nether";
		Dimensions.NETHER.schems.plans = new String[0];
		allis.add(Dimensions.NETHER.schems);

		Dimensions.END.schems = new IslandShematics();
		Dimensions.END.schems.name = "end";
		Dimensions.END.schems.plans = new String[0];
		allis.add(Dimensions.END.schems);

//		DimensionType.CLOUD.schems = new IslandShematics();
//		DimensionType.CLOUD.schems.name = "clouds";
//		DimensionType.CLOUD.schems.plans = new String[0];
//		allis.add(DimensionType.CLOUD.schems);

		for(IslandShematics isc : allis){
			getLogger().info("Chargement de l'ile "+isc.name+"...");

			File f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name);
			if(f.isDirectory()){

				f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name+"/ile.schematic");
				if(f.exists()) isc.island = ClipboardFormat.SCHEMATIC.load(f);
				else throw new Exception("Pas de mini-iles dans l'ile "+isc.name);

				f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name+"/minis");
				File[] fi = f.listFiles();
				if(fi==null)throw new Exception("Pas de mini-iles dans l'ile "+isc.name);
				isc.miniIslands = new Schematic[fi.length];
				for (int i=0;i<fi.length;i++) {
					isc.miniIslands[i] = ClipboardFormat.SCHEMATIC.load(fi[i]);
				}

				f = new File(Main.main.getDataFolder()+"/islands/schems/"+isc.name+"/structures");
				fi = f.listFiles();
				if(fi==null)throw new Exception("Pas de structures dans l'ile "+isc.name);
				isc.structures = new Schematic[isc.plans.length];
				for (File f2 : fi) {
					String a = f2.getName().split("\\.")[0].toLowerCase();
					int id = isc.indexOf(a);
					if(id==-1)throw new Exception("Structure "+a+" inconnue pour l'ile "+isc.name);
					isc.structures[id] = ClipboardFormat.SCHEMATIC.load(f2);
				}

				for(int i=0;i<isc.structures.length;i++){
					if(isc.structures[i]==null)throw new Exception("La structure "+isc.plans[i]+" est manquante pour l'ile "+isc.name);
				}


			}else throw new Exception("Pas de dossier pour l'ile "+isc.name);
			getLogger().info("Ile "+isc.name+" chargée avec succès !");
		}
	}
}
