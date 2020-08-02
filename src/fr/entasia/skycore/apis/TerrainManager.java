package fr.entasia.skycore.apis;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.FaweCache;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.registry.WorldData;
import fr.entasia.apis.other.CodePasser;
import fr.entasia.apis.other.Pair;
import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.apis.utils.TextUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.objs.IslandShematics;
import fr.entasia.skycore.objs.isutils.BlockType;
import fr.entasia.skycore.others.enums.Dimensions;
import fr.entasia.skycore.others.enums.IslandType;
import fr.entasia.skycore.others.enums.MemberRank;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

	/*
	400/16 = 25 chunks par île, ca tombe pile. Faudra faire un système si jamais le diamètre de l'île est pas un nombre entier de chunks (réfléchis)
	 */

public class TerrainManager {

	private static final BaseBlock bedrockBlock = FaweCache.getBlock(7, 0);
	private static final BaseBlock airBlock = FaweCache.getBlock(0, 0);
	public static HashMap<Material, BlockType> blockValues = new HashMap<>();
	private static final Random r = new Random();

	private static int getRandom() {
		int possibilities = Utils.ISSIZE - 30 - 20;
		int a = r.nextInt(possibilities); // 0-349 = 350 possibilités
		if (a >= possibilities /2) a += 30;
		return a+10;
	}

	public static EditSession getSession(World w){
		return new EditSessionBuilder(w.getName()).fastmode(true).allowedRegionsEverywhere().build();
	}

	protected static ArrayList<ChunkSnapshot> getChunks(BaseIsland is, Dimensions dim){
		return getChunks(is.getMinXBuild(), is.getMaxXBuild(), is.getMinZBuild(), is.getMaxZBuild(), dim);
	}

	protected static ArrayList<ChunkSnapshot> getChunks(ISID isid, Dimensions dim){
		return getChunks(isid.getMinXTotal(), isid.getMaxXTotal(), isid.getMinZTotal(), isid.getMaxZTotal(), dim);
	}

	protected static ArrayList<ChunkSnapshot> getChunks(int minx, int maxx, int minz, int maxz, Dimensions dim){
		ServerUtils.wantMainThread();
		ArrayList<ChunkSnapshot> chunks = new ArrayList<>();

		Chunk ch;
		for(int x=minx;x<maxx;x+=16){
			for(int z=minz;z<maxz;z+=16) {
				ch = dim.world.getBlockAt(x, 70, z).getChunk();
				if (ch.isLoaded()) chunks.add(ch.getChunkSnapshot());
				else {
					ch.load();
					chunks.add(ch.getChunkSnapshot());
					ch.unload();
				}
			}
		}
		return chunks;
	}

	// LE BORDEL COMMENCE ICI


	public static void generateIsland(SkyPlayer sp, IslandType type){
		if(sp.getIslands().size()>5) {
			sp.p.sendMessage("§cTu as déja trop d'îles ! Quitte en pour pouvoir en créer une");
			return;
		}
		int ts = (int) (System.currentTimeMillis()/1000);
		int a = 60*60*24*3-(ts-sp.lastGenerated);
		if(a>0){ // 3 jours
			sp.p.sendMessage("§cTu dois encore attendre "+ TextUtils.secondsToTime(a)+" pour générer une nouvelle île !");
			return;
		}
		sp.setLastGenerated(ts);
		sp.p.sendMessage("§eGénération de ton île en cours.. Merci de patienter !");

		new BukkitRunnable(){
			@Override
			public void run() {
				try {

					ISID isid = CooManager.findFreeSpot();
					ISPLink link = new ISPLink(new BaseIsland(isid, type), sp, MemberRank.CHEF);
					BaseAPI.registerIsland(link.is, sp);


					genOW(isid, type);
					sp.p.sendMessage("§6Génération overworld terminée !");
//					genDimension(link.is, Dimensions.NETHER.world, Dimensions.NETHER.schems);
//					sp.p.sendMessage("§6Génération nether terminée !");
//					genDimension(link.is, Dimensions.END.world, Dimensions.END.schems);
//					sp.p.sendMessage("§6Génération end terminée !");

					new BukkitRunnable() {
						@Override
						public void run() {
							calcPoints(link.is, new CodePasser.Arg<Integer>() {
								@Override
								public void run(Integer rem) {
									link.is.setMalus((int) link.is.rawpoints);
									sp.p.sendMessage("§aFin de création de ton île ! Téléportation au cours.. §eBonne aventure !");
									sp.p.teleport(link.is.getHome());
								}
							});
						}
					}.runTask(Main.main);

				}catch(Throwable e){
					e.printStackTrace();
					sp.p.sendMessage("§cUne erreur s'est produite lors de la création de ton île ! Contacte un membre du Staff");
				}
			}
		}.runTaskAsynchronously(Main.main);
	}

	public static void genOW(ISID isid, IslandType type) throws Exception {
		EditSession session = getSession(Dimensions.OVERWORLD.world);
		genBaseDimension(isid, session, Dimensions.OVERWORLD.world, type.schems);
		setBiome(isid, Dimensions.OVERWORLD.world, type.biome);

		// Ile géante

		Vector loc = new Vector(isid.getMinXTotal(), 70, isid.getMinZTotal());
		switch (r.nextInt(4)) {
			case 0:
				loc.mutX(loc.getX() + 50);
				loc.mutZ(loc.getZ() + r.nextInt(Utils.ISSIZE-100)+50);
				break;
			case 1:
				loc.mutX(loc.getX() + Utils.ISSIZE - 50);
				loc.mutZ(loc.getZ() + r.nextInt(Utils.ISSIZE-100)+50);
				break;
			case 2:
				loc.mutX(loc.getX() + r.nextInt(Utils.ISSIZE-100)+50);
				loc.mutZ(loc.getZ() + 50);
				break;
			case 3:
				loc.mutX(loc.getX() + r.nextInt(Utils.ISSIZE-100)+50);
				loc.mutZ(loc.getZ() + Utils.ISSIZE - 50);
				break;
		}
		type.schems.structures[0].paste(session, loc, false);
		session.flushQueue();

	}

	public static void genNether(ISID isid) throws Exception {
		EditSession session = getSession(Dimensions.NETHER.world);
		genBaseDimension(isid, session, Dimensions.NETHER.world, Dimensions.NETHER.schems);


	}

	public static void genEnd(ISID isid) throws Exception {
		EditSession session = getSession(Dimensions.END.world);
		genBaseDimension(isid, session, Dimensions.END.world, Dimensions.END.schems);
	}

	protected static void calcPoints(BaseIsland is, CodePasser.Arg<Integer>
			code){
		ServerUtils.wantMainThread();
		ArrayList<ChunkSnapshot> chunks = getChunks(is.isid, Dimensions.OVERWORLD);

		new BukkitRunnable() {
			@Override
			public void run() {
				Material m;
				long points = 0;
				for (ChunkSnapshot cs : chunks) {
					for (int x = 0; x <= 16; x++) {
						for (int y = 0; y < 256; y++) {
							for (int z = 0; z <= 16; z++) {
								m = cs.getBlockType(x, y, z);
//										cs.getBlockData(x, y, z));
								if (m != Material.AIR) {
									BlockType bt = blockValues.get(m);
									if(bt!=null){
										points+=bt.getPrice(cs.getBlockData(x, y, z));
									}
								}
							}
						}
					}
				}

				is.rawpoints = points-is.malus;
				Pair<Integer, Integer> p = calcLevel(is.rawpoints);
				// TODO UPDATE LVL ET REM_POINTS
				is.lvl = p.key; // temporaire
				if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_islands SET rawpoints = ? WHERE x=? and z=?", is.rawpoints, is.isid.x, is.isid.z);
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							code.run(p.value);
						}catch(Throwable e){
							e.printStackTrace();
						}
					}
				}.runTask(Main.main);
			}
		}.runTaskAsynchronously(Main.main);
	}

	public static Pair<Integer, Integer> calcLevel(long raw){
		int lvl = -1;
		int rem = 5000;
		while(raw>0){
			lvl++;
			raw-=rem;
			if(rem>100000)rem+=10000;
			else rem*=1.1;
		}
		return new Pair<>(lvl, (int)-raw);
	}

	public static boolean clearTerrain(ISID isid, EditSession editSession){
		ServerUtils.wantChildThread();
		try{
			editSession.setBlocks(new CuboidRegion(
					new Vector(isid.getMinXTotal(), 0, isid.getMinZTotal()),
					new Vector(isid.getMaxXTotal(), 255, isid.getMaxZTotal())), airBlock);
			editSession.flushQueue();
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public static void setBiome(ISID isid, World w, Biome biome){
		int minx = isid.getMinXTotal();
		int maxx = isid.getMaxXTotal();
		int minz = isid.getMinZTotal();
		int maxz = isid.getMaxZTotal();

		for(int x=minx;x<=maxx;x++){
			for(int z=minz;z<=maxz;z++){
				w.setBiome(x, z, biome);
			}
		}
	}


	protected static void genBaseDimension(ISID isid, EditSession session, World w, IslandShematics isc) throws Exception {
		ServerUtils.wantChildThread();

		Vector bloc = new Vector(isid.getMinXTotal(), 70, isid.getMinZTotal());
		Vector loc = new Vector(bloc);

		// FILL
		clearTerrain(isid, session);



		// GENERATION ILE NORMALE

		loc.mutX(isid.getMiddleX());
		loc.mutZ(isid.getMiddleZ());
		isc.island.paste(session, loc, false); // ca marche

		session.setBlock(loc.setY(65), bedrockBlock);

		session.flushQueue();

		// GENERATION MINI ILES

		int j = 0;
		WorldData wd = FaweAPI.getWorld(w.getName()).getWorldData();
		AffineTransform transform = new AffineTransform();
		for (int i = 0; i < 50; i++) {
			loc.mutX(bloc.getX() + getRandom());
			loc.mutZ(bloc.getZ() + getRandom());

			isc.miniIslands[j].paste(session, wd, loc, false, transform.rotateY(90*r.nextInt(4)));
			j++;
			if (j == isc.miniIslands.length)j = 0;
		}
		session.flushQueue();

	}
}
