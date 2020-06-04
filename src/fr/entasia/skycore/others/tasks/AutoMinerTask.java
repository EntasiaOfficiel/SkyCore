package fr.entasia.skycore.others.tasks;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.objs.AutoMiner;
import org.bukkit.Material;
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

			temp = new ArrayList<>();
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
					temp.removeIf((am)->{ // boucle
						// TODO ADD DIRECT AU HOPPER

						if(toMine.contains(am.toBreak.getType())){
							for(ItemStack drop : am.toBreak.getDrops(am.pickaxe)){
								am.toBreak.getWorld().dropItem(am.toBreak.getLocation(), drop);
							}
							am.toBreak.setType(Material.AIR);

							short dura = (short) (am.pickaxe.getDurability()+2);
							if(dura>am.pickaxe.getType().getMaxDurability()){
								am.hopper.setType(Material.AIR);
								am.delete();
								return true;
							}else am.pickaxe.setDurability(dura);
							return false;
						}else return false;
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
