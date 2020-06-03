package fr.entasia.skycore.objs;

import fr.entasia.skycore.others.tasks.AutoMinerTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class AutoMiner {

	public static final byte MAX = 10;

	public Block center;
	public ItemStack pickaxe;

	public ArmorStand[] armorStands = new ArmorStand[4];

	public AutoMiner(Block center, ItemStack pickaxe){
		this.center = center;
		this.pickaxe = pickaxe;
	}


	public void delete(boolean dropItem){
		// TODO REMOVE FROM ARRAYLIST
		if(dropItem){
			center.getWorld().dropItem(center.getLocation(), pickaxe);
			pickaxe = null; // au cas ou pour erreurs
		}
		for(ArmorStand as : armorStands){
			as.remove();
		}
	}

	public void spawn(){
		Location loc = center.getLocation().add(0.5, -0.8, 0.5);
		for(int i=0;i<4;i++){
			loc.setYaw(-90+i*90);
			armorStands[i] = (ArmorStand) center.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

			armorStands[i].setCustomName("AMPickaxe");

			armorStands[i].setInvulnerable(true);
			armorStands[i].setVisible(false);
			armorStands[i].setMarker(true);

			armorStands[i].setRemoveWhenFarAway(false);
			armorStands[i].setCanMove(false);
			armorStands[i].setGravity(false);
			armorStands[i].setBasePlate(false);
			armorStands[i].setRightArmPose(AutoMinerTask.baseAngle);

			armorStands[i].setItemInHand(pickaxe);
		}
	}
}
