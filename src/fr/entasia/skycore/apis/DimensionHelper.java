package fr.entasia.skycore.apis;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.others.enums.Dimension;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

//@Deprecated
public class DimensionHelper {

	protected static void findNetherPortal(BaseIsland is, Player p, Dimension in) {
		ArrayList<ChunkSnapshot> chunks = TerrainManager.getChunks(is, in);
		new BukkitRunnable() {
			@Override
			public void run() {
				final boolean x_axis;
				Location loc = p.getLocation();
				if (loc.getBlock().getType() == Material.PORTAL &&
						(loc.clone().add(0, 0, 1).getBlock().getType() == Material.PORTAL ||
								loc.clone().add(0, 0, -1).getBlock().getType() == Material.PORTAL)) {
					x_axis = true;
				} else x_axis = false;

				Material type;
				for (ChunkSnapshot cs : chunks) {
					for (int x = 0; x <= 16; x++) {
						for (int y = 0; y < 256; y += 3) {
							for (int z = 0; z <= 16; z++) {
								type = cs.getBlockType(x, y, z);
								if (type == Material.PORTAL) { // PORTAIL TROUVE : DETECTION SENS
									int finalX = x;
									int finalY = y;
									int finalZ = z;
									new BukkitRunnable() {
										@Override
										public void run() {
											Location finalLoc = new Location(in.world, cs.getX() * 16 + finalX, finalY, cs.getZ() * 16 + finalZ,
													loc.getYaw(), loc.getPitch());
											boolean dest_x_axis;
											if (finalLoc.clone().add(0, 0, 1).getBlock().getType() == Material.PORTAL || finalLoc.clone().add(0, 0, -1).getBlock().getType() == Material.PORTAL) {
												dest_x_axis = true;
											} else dest_x_axis = false;
											if (dest_x_axis != x_axis) {
												float yaw = finalLoc.getYaw();
												yaw += 90;
												finalLoc.setYaw(yaw);
											}
											while (finalLoc.getBlock().getType() == Material.PORTAL) {
												finalLoc = finalLoc.add(0, -1, 0);
											}
											finalLoc = finalLoc.add(0.5, 1.2, 0.5);
											if (in == Dimension.OVERWORLD) is.OWNetherPortal = finalLoc;
											else if (in == Dimension.NETHER) is.netherPortal = finalLoc;
											else{
												InternalAPI.warn("Dimension invalide !");
												return;
											}
											p.teleport(finalLoc);
										}
									}.runTask(Main.main);
									return;
								}
							}
						}
					}
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						if(in==Dimension.OVERWORLD)p.teleport(is.getHome());
						else if(in==Dimension.NETHER) p.teleport(is.isid.getMiddleLoc(Dimension.NETHER));
						else InternalAPI.warn("Dimension invalide !");
					}
				}.runTask(Main.main);
			}
		}.runTaskAsynchronously(Main.main);
	}

	protected static void findEndPortal(BaseIsland is, Player p, Dimension in) {
		ArrayList<ChunkSnapshot> chunks = TerrainManager.getChunks(is, in);
		new BukkitRunnable() {
			@Override
			public void run() {
				Material type;
				for (ChunkSnapshot cs : chunks) {
					for (int x = 0; x <= 16; x++) {
						for (int y = 0; y < 256; y += 3) {
							for (int z = 0; z <= 16; z++) {
								type = cs.getBlockType(x, y, z);
								if (type == Material.ENDER_PORTAL) {
									Location loc = new Location(in.world, cs.getX() * 16 + x, y, cs.getZ() * 16 + z,
											p.getLocation().getYaw(), p.getLocation().getPitch());

									if(loc.clone().add(1, 0, 0).getBlock().getType()==Material.ENDER_PORTAL_FRAME){
										loc.add(-1, 0, 0);
									}else if(loc.clone().add(-1, 0, 0).getBlock().getType()==Material.ENDER_PORTAL_FRAME) {
										loc.add(1, 0, 0);
									}

									while(loc.getBlock().getType()==Material.ENDER_PORTAL){
										loc.add(0, 0, 1);
									}

									Location finalLoc = loc.add(0.5, 0.2, 1.5);
									new BukkitRunnable() {
										@Override
										public void run() {
											if (in == Dimension.OVERWORLD) is.OWNetherPortal = finalLoc;
											else if (in == Dimension.END) is.endPortal = finalLoc;
											else{
												InternalAPI.warn("Dimension invalide !");
												return;
											}
											finalLoc.getBlock().setType(Material.END_GATEWAY);
											finalLoc.add(0, 1, 0).getBlock().setType(Material.END_GATEWAY);
											p.teleport(finalLoc);
										}
									}.runTask(Main.main);
									return;
								}
							}
						}
					}
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						if(in==Dimension.OVERWORLD)p.teleport(is.getHome());
						else if(in==Dimension.END)p.teleport(is.isid.getMiddleLoc(Dimension.END));
						else InternalAPI.warn("Dimension invalide ! "+in);
					}
				}.runTask(Main.main);
			}
		}.runTaskAsynchronously(Main.main);
	}

	@Deprecated
	public static void enableDimension(BaseIsland is, Dimension dim) throws Throwable {

//		String c;
//		switch(dim){
//			case NETHER:{
//				is.hasNether = true;
//				c = "hasNether";
//				break;
//			}
//			case END:{
//				is.hasEnd = true;
//				c = "hasEnd";
//				break;
//			}
//			default:{
//				throw new EntasiaException("Invalid dimension");
//			}
//		}
//
//		TerrainManager.genDimension(is, dim.world, dim.schems);
//
//		if(InternalAPI.SQLEnabled())Main.sqlConnection.fastUpdate("UPDATE sky_islands set "+c+"=1 WHERE x = ? and z = ?", is.isid.x, is.isid.z);
	}


}
