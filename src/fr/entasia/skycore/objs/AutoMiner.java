package fr.entasia.skycore.objs;

import fr.entasia.skycore.others.tasks.AutoMinerTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AutoMiner {

	public static final byte MAX = 10;

	public Block hopper;
	public Block ore;
	public ItemStack pickaxe;

	public ArmorStand[] armorStands = new ArmorStand[4];

	public AutoMiner(Block hopper, ItemStack pickaxe){
		this.hopper = hopper;
		this.ore = hopper.getRelative(BlockFace.UP);
		this.pickaxe = pickaxe;
	}


	public static void deleteByBlock(Block b){
		Location loc = b.getLocation().add(0.5, 0.5, 0.5);
		System.out.println(loc);
		for(Entity ent : loc.getNearbyEntitiesByType(ArmorStand.class, 0.4)) {
			if ("AMPickaxe".equals(ent.getCustomName())) {
				ent.remove();
			}
		}
	}

	public void delete(){
		deleteByBlock(hopper);
	}

	private static final Vector[] dirs = new Vector[4];

	static{
		dirs[0] = new Vector(1, 0, 0);
		dirs[1] = new Vector(0, 0, 1);
		dirs[2] = new Vector(-1, 0, 0);
		dirs[3] = new Vector(0, 0, -1);

	}

	public void spawn(){
		Location loc = hopper.getLocation().add(0.5, -0.3, 0.5);
		for(int i=0;i<4;i++){
			loc.setYaw(i*90);
			armorStands[i] = (ArmorStand) hopper.getWorld().spawnEntity(loc.clone().add(dirs[i].multiply(0.3)), EntityType.ARMOR_STAND);

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
