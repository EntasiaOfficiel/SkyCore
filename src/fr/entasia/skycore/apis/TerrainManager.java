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
import fr.entasia.apis.ServerUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.otherobjs.CodePasser;
import fr.entasia.skycore.otherobjs.IslandShematics;
import fr.entasia.skycore.otherobjs.islevel.BlockType;
import fr.entasia.skycore.others.enums.Dimension;
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
	INFOS :
	25 chunks par île, ca tombe pile. Faudra faire un système si jamais le diamètre de l'île est pas un nombre entier de chunks (réfléchis)
	 */

public class TerrainManager {



	public static void generateIsland(SkyPlayer sp, IslandType type){
		if(sp.getIslands().size()>10||sp.generating)return;
		sp.generating = true;
		sp.p.sendMessage("§eGénération de votre île en cours.. merci de patienter !");

		new BukkitRunnable(){
			@Override
			public void run() {
				try {

					ISID isid = CooManager.findFreeSpot();
					ISPLink link = new ISPLink(new BaseIsland(isid, type), sp, MemberRank.CHEF);
					BaseAPI.registerIsland(link.is, sp);


					genDimension(link.is, Dimension.OVERWORLD.world, type.schems);
					setBiome(link.is, Dimension.OVERWORLD.world, link.is.type.biome);
					sp.p.sendMessage("§6Génération overworld terminée !");
//					genDimension(link.is, Dimensions.NETHER.world, Dimensions.NETHER.schems);
//					sp.p.sendMessage("§6Génération nether terminée !");
//					genDimension(link.is, Dimensions.END.world, Dimensions.END.schems);
//					sp.p.sendMessage("§6Génération end terminée !");

					new BukkitRunnable() {
						@Override
						public void run() {
							calcPoints(link.is, new CodePasser.Void() {
								@Override
								public void run() {
									link.is.setMalus((int) link.is.rawpoints);
									sp.p.sendMessage("§aFin de création de ton île ! Téléportation au cours.. §eBonne aventure !");
									sp.p.teleport(link.is.getHome());
									sp.generating = false;
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

	protected static ArrayList<ChunkSnapshot> getChunks(BaseIsland is, Dimension dim){
		return getChunks(is.getMinXBuild(), is.getMaxXBuild(), is.getMinZBuild(), is.getMaxZBuild(), dim);
	}

	protected static ArrayList<ChunkSnapshot> getChunks(ISID isid, Dimension dim){
		return getChunks(isid.getMinXTotal(), isid.getMaxXTotal(), isid.getMinZTotal(), isid.getMaxZTotal(), dim);
	}

	protected static ArrayList<ChunkSnapshot> getChunks(int minx, int maxx, int minz, int maxz, Dimension dim){
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

	public static HashMap<Material, BlockType> blockValues = new HashMap<>();

	protected static void calcPoints(BaseIsland is, CodePasser.Void code){
		ServerUtils.wantMainThread();
		ArrayList<ChunkSnapshot> chunks = getChunks(is.isid, Dimension.OVERWORLD);

		new BukkitRunnable() {
			@Override
			public void run() {
				int internal=0;
				Material m;
				long points = 0;
				for (ChunkSnapshot cs : chunks) {
					internal++;
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
				// TODO UPDATE LVL ET REM_POINTS
				is.lvl = is.rawpoints; // temporaire
				if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands SET rawpoints = ? WHERE x=? and z=?", is.rawpoints, is.isid.x, is.isid.z);
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							code.run();
						}catch(Throwable e){
							e.printStackTrace();
						}
					}
				}.runTask(Main.main);
			}
		}.runTaskAsynchronously(Main.main);
	}

	private static Random r = new Random();

	private static int getRandom() {
		int possibilities = Utils.ISSIZE - 30 - 20;
		int a = r.nextInt(possibilities); // 0-349 = 350 possibilités
		if (a >= possibilities /2) a += 30;
		return a+10;
	}

	public static EditSession getSession(World w){
		return new EditSessionBuilder(w.getName()).fastmode(true).allowedRegionsEverywhere().build();
	}

	private static BaseBlock airBlock = FaweCache.getBlock(0, 0);

	protected static boolean clearTerrain(BaseIsland is, EditSession editSession){
		ServerUtils.wantChildThread();
		try{
			editSession.setBlocks(new CuboidRegion(
					new Vector(is.isid.getMinXTotal(), 0, is.isid.getMinZTotal()),
					new Vector(is.isid.getMaxXTotal(), 255, is.isid.getMaxZTotal())), airBlock);
			editSession.flushQueue();
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public static void setBiome(BaseIsland is, World w, Biome biome){
		int minx = is.isid.getMinXTotal();
		int maxx = is.isid.getMaxXTotal();
		int minz = is.isid.getMinZTotal();
		int maxz = is.isid.getMaxZTotal();

		for(int x=minx;x<=maxx;x++){
			for(int z=minz;z<=maxz;z++){
				w.setBiome(x, z, biome);
			}
		}
	}

	private static BaseBlock bedrockBlock = FaweCache.getBlock(7, 0);

	protected static void genDimension(BaseIsland is, World w, IslandShematics isc) throws Throwable {
		ServerUtils.wantChildThread();

		Vector bloc = new Vector(is.isid.getMinXTotal(), 70, is.isid.getMinZTotal());
		Vector loc = new Vector(bloc);

		// FILL OVERWORLD
		EditSession editSession = getSession(w);
		clearTerrain(is, editSession);



		//				 GENERATION ILE NORMALE

		loc.mutX(is.isid.getMiddleX());
		loc.mutZ(is.isid.getMiddleZ());
		isc.island.paste(editSession, loc, false); // ca marche

		editSession.setBlock(loc.setY(65), bedrockBlock);

		editSession.flushQueue();

		// GENERATION MINI ILES

		int j = 0;
		WorldData wd = FaweAPI.getWorld(w.getName()).getWorldData();
		AffineTransform transform = new AffineTransform();
		for (int i = 0; i < 70; i++) {
			loc.mutX(bloc.getX() + getRandom());
			loc.mutZ(bloc.getZ() + getRandom());

			isc.miniIslands[j].paste(editSession, wd, loc, false, transform.rotateY(90*r.nextInt(4)));
			j++;
			if (j == isc.miniIslands.length)j = 0;
		}

		editSession.flushQueue();

		// GENERATION OTHERS :
		if(w== Dimension.NETHER.world){

		}else if(w== Dimension.END.world){

		}else{
			// Ile géante

			loc = new Vector(bloc);
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
			isc.structures[0].paste(editSession, loc, false);

		}
		editSession.flushQueue();

	}

}
