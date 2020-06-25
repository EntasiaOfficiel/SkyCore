package fr.entasia.skycore.others.tasks;

import fr.entasia.apis.utils.ServerUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.CooManager;
import fr.entasia.skycore.objs.AutoMiner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;

public class AutoMinerTask extends BukkitRunnable {

	public static final double adder = Math.PI / 5;
	public static final EulerAngle baseAngle = new EulerAngle(Math.toRadians(-32), Math.toRadians(90), Math.toRadians(0));
	public static ArrayList<AutoMiner> miners = new ArrayList<>();

	public static final ArrayList<Material> toMine = new ArrayList<>();

	static{
		toMine.add(Material.STONE);
		toMine.add(Material.COBBLESTONE);
		toMine.add(Material.COAL_ORE);
		toMine.add(Material.IRON_ORE);
		toMine.add(Material.GOLD_ORE);
		toMine.add(Material.LAPIS_ORE);
		toMine.add(Material.REDSTONE_ORE);
		toMine.add(Material.DIAMOND_ORE);
		toMine.add(Material.EMERALD_ORE);
		toMine.add(Material.QUARTZ_ORE);
		toMine.add(Material.NETHERRACK);
	}


	/*
	0 = reloading
	1 = up
	2 = down


	levé : -50
	baissé : -32
	 */

	ArrayList<AutoMiner> temp = new ArrayList<>();

	@Override
	public void run() {
		try{

			temp.clear();
			for(AutoMiner am : miners){
				if(toMine.contains(am.toBreak.getType())){
					temp.add(am);
				}
			}

			// montée
			EulerAngle angle = baseAngle;
			double f = adder/15;
			for(int i=0;i<15;i++){
				angle = angle.subtract(f, 0, 0);
				for(AutoMiner am : temp){
					for(ArmorStand as : am.armorStands){
						as.setRightArmPose(angle);
					}
				}
				Thread.sleep(60);
			}

			if(!Main.main.isEnabled()){
				cancel();
				return;
			}
			new BukkitRunnable(){

				@Override
				public void run() {
					temp.removeIf((am) -> { // boucle
						// TODO ADD DIRECT AU HOPPER

						if (am.pickaxe == null) return true;
						else if (am.hopper.getType() == Material.HOPPER) {


							if(am.hopper.getRelative(BlockFace.DOWN).getType().isTransparent()){
								Location loc = am.hopper.getLocation();
								BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(loc));
								String sloc = "§cxyz : §6"+loc.getBlockX()+"§c;§6"+loc.getBlockY()+"§c;§6"+loc.getBlockZ();
								if(is==null){
									ServerUtils.permMsg("log.autominer", "§cUn Autominer à un block transparent au dessous de lui !"+
											"(Cela cause du lag)\n"+ sloc+"§c. Monde "+loc.getWorld().getName());
								}else{
									is.sendTeamMsg("§cUn Autominer à un block transparent au dessous de lui !"+
											"(Cela cause du lag)\n"+ sloc+"§c. Dimension : §6"+loc.getWorld().getEnvironment().name());
								}
							}

							if (toMine.contains(am.toBreak.getType())) {
								am.toBreak.breakNaturally(am.pickaxe);
								//							CraftBlock b;
								//							b.setType();

								short dura = (short) (am.pickaxe.getDurability() + 2);
								if (dura > am.pickaxe.getType().getMaxDurability()) {
									am.hopper.setType(Material.AIR);
									am.delete();
									AutoMinerTask.miners.remove(am);
									return true;
								} else am.pickaxe.setDurability(dura);
								return false;
							} else return false;
						} else {
							am.hopper.getWorld().dropItem(am.hopper.getLocation(), am.pickaxe);
							am.pickaxe = null;
							am.delete();
							AutoMinerTask.miners.remove(am);
							return true;
						}
					});
				}
			}.runTask(Main.main);

			Thread.sleep(1000);

			// Descente
			f = adder/15;
			for(int i=0;i<15;i++){
				angle = angle.add(f, 0, 0);
				for(AutoMiner am : temp){
					for(ArmorStand as : am.armorStands){
						as.setRightArmPose(angle);
					}
				}
				Thread.sleep(60);
			}

			for(AutoMiner am : miners){
				for(ArmorStand as : am.armorStands){
					as.setRightArmPose(baseAngle);
				}
			}


		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
}
