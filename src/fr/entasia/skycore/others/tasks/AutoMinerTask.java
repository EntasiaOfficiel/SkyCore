package fr.entasia.skycore.others.tasks;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.objs.AutoMiner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;

public class AutoMinerTask extends BukkitRunnable {

	public static final double adder = Math.PI / 6;
	public static final EulerAngle baseAngle = new EulerAngle(Math.toRadians(-32), Math.toRadians(90), Math.toRadians(0));
	public static ArrayList<AutoMiner> miners = new ArrayList<>();

	/*
	0 = reloading
	1 = up
	2 = down


	levé : -50
	baissé : -32
	 */

	@Override
	public void run() {
		try{


			// montée
			EulerAngle angle = baseAngle;
			double f = adder/10;
			for(int i=0;i<10;i++){
				angle = angle.subtract(f, 0, 0);
				for(AutoMiner am : miners){
					for(ArmorStand as : am.armorStands){
						as.setRightArmPose(angle);
					}
				}
				Thread.sleep(60);
			}

			new BukkitRunnable(){

				@Override
				public void run() {
					short dura;
					for(AutoMiner am : miners){
						// TODO ADD DIRECT AU HOPPER
						am.center.getDrops(am.pickaxe);
						dura = (short) (am.pickaxe.getDurability()+15);
						if(dura>am.pickaxe.getType().getMaxDurability()){
							am.center.setType(Material.AIR);
							am.delete(false);
						}else am.pickaxe.setDurability(dura);
					}
				}
			}.runTask(Main.main);

			Bukkit.broadcastMessage("montée finie");
			Thread.sleep(500);
			Bukkit.broadcastMessage("descente");

			// Descente
			angle = baseAngle;
			f = adder/10;
			for(int i=0;i<10;i++){
				angle = angle.add(f, 0, 0);
				for(AutoMiner am : miners){
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
