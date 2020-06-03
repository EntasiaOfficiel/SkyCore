package fr.entasia.skycore.events;

import fr.entasia.apis.Serialization;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.objs.AutoMiner;
import fr.entasia.skycore.others.tasks.AutoMinerTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class AMEvents implements Listener {


	public static ArrayList<Material> pickaxes = new ArrayList<>();

	static {
		pickaxes.add(Material.DIAMOND_PICKAXE);
		pickaxes.add(Material.GOLD_PICKAXE);
		pickaxes.add(Material.IRON_PICKAXE);
		pickaxes.add(Material.STONE_PICKAXE);
		pickaxes.add(Material.WOOD_PICKAXE);
	}


	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if(e.getAction()== Action.RIGHT_CLICK_BLOCK&&e.getClickedBlock().getType()== Material.HOPPER&&e.getPlayer().isSneaking()){
			ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
			if(pickaxes.contains(item.getType())){

				BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(e.getClickedBlock().getLocation()));
				if(is==null)return;
				if(is.getMember(e.getPlayer().getUniqueId())==null){
					e.getPlayer().sendMessage("§cTu n'es pas membre de cette île !");
				}else if (is.autominers.size()>=AutoMiner.MAX) {
					e.getPlayer().sendMessage("§cCette île à déja atteint son maximum de mineur automatiques ! (" + AutoMiner.MAX + ")");
				}else{
					e.setCancelled(true);
					for(AutoMiner am : is.autominers){
						if(e.getClickedBlock()==am.hopper){
							e.getPlayer().getInventory().setItemInMainHand(am.pickaxe);
							am.pickaxe = item;
							return;
						}
					}

					e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
					AutoMiner am = new AutoMiner(e.getClickedBlock(), item);
					AutoMiner.deleteByBlock(e.getClickedBlock());
					am.spawn();

					AutoMinerTask.miners.add(am);
					is.autominers.add(am);
					Location loc = am.hopper.getLocation();
					Main.sqlite.fastUpdate("INSERT INTO autominers (is_x, is_z, x, y, z, world, item) VALUES (?, ?, ?, ?, ?, ?, ?)",
							is.isid.x, is.isid.z, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), Serialization.serialiseItem(item));
				}
			}
		}
	}

	@EventHandler
	public void a(BlockBreakEvent e){
		if(e.getBlock().getType()==Material.HOPPER){
			BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(e.getBlock().getLocation()));
			if(is==null)return;
			for(AutoMiner am : is.autominers){
				if(e.getBlock()==am.hopper){
					am.delete();
					am.ore.getWorld().dropItem(am.ore.getLocation(), am.pickaxe);
				}
			}
		}
	}

	@EventHandler
	public void a(PlayerArmorStandManipulateEvent e){
		if("AMPickaxe".equals(e.getRightClicked().getCustomName())){
			e.setCancelled(true);
		}
	}
}
