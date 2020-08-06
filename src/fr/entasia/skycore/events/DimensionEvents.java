package fr.entasia.skycore.events;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.objs.enums.Dimensions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DimensionEvents implements Listener {

	@EventHandler
	public void a(EntityTeleportEndGatewayEvent e) { // gl
		e.setCancelled(true);
	}

	@EventHandler
	public void a(PlayerTeleportEndGatewayEvent e) { // gl
		e.setCancelled(true);
	}

	@EventHandler
	public void dimension(PlayerPortalEvent e){ // gl
		e.setCancelled(true);
		Location loc = e.getFrom();
		Location loc2 = loc.clone().add(0, 1, 0);
		BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(loc));
		if(is!=null){
			ISPLink link = is.getMember(e.getPlayer().getUniqueId());
			if(link!=null){
				if (loc.getWorld()== Dimensions.OVERWORLD.world) {
					if (loc.getBlock().getType() == Material.PORTAL||loc2.getBlock().getType()==Material.PORTAL){ // TP NETHER
						if(is.hasDimension(Dimensions.NETHER)){
							is.teleportNether(link.sp.p);
						}else{
							if(link.is.dimGen)return;
							link.is.dimGen = true;
							new BukkitRunnable() {
								@Override
								public void run() {
									link.sp.p.sendMessage("§6Activation de la dimension Nether...");
									if(InternalAPI.allowDimension(is, Dimensions.NETHER)){
										link.sp.p.sendMessage("§aFini !");
										Bukkit.getScheduler().callSyncMethod(Main.main, ()->is.teleportNether(link.sp.p));
									}else link.sp.p.sendMessage("§cErreur d'activation de dimension ! Contacte un Membre du Staff !");
									link.is.dimGen = false;
								}
							}.runTaskAsynchronously(Main.main);
						}
					} else if (loc.getBlock().getType() == Material.ENDER_PORTAL||loc2.getBlock().getType()==Material.ENDER_PORTAL){ // TP END
						if(is.hasDimension(Dimensions.END)){
							is.teleportEnd(link.sp.p);
						}else{
							if(link.is.dimGen)return;
							link.is.dimGen = true;
							new BukkitRunnable() {
								@Override
								public void run() {
									link.sp.p.sendMessage("§6Activation de la dimension End...");
									if(InternalAPI.allowDimension(is, Dimensions.END)){
										link.sp.p.sendMessage("§aFini !");
										Bukkit.getScheduler().callSyncMethod(Main.main, ()->is.teleportEnd(link.sp.p));
									}else link.sp.p.sendMessage("§cErreur d'activation de dimension ! Contacte un Membre du Staff !");
									link.is.dimGen = false;
								}
							}.runTaskAsynchronously(Main.main);
						}
					}


				} else if (loc.getWorld()== Dimensions.NETHER.world) {
					if (loc.getBlock().getType() == Material.PORTAL||loc2.getBlock().getType()==Material.PORTAL){
						is.teleportOW(Dimensions.NETHER, link.sp.p);
					} else link.sp.p.sendMessage("§cPortail invalide !");
				} else if (loc.getWorld()== Dimensions.END.world) {
					if(link.is.hasDimension(Dimensions.END)){
						if (loc.getBlock().getType() == Material.ENDER_PORTAL||loc2.getBlock().getType()==Material.ENDER_PORTAL){
							is.teleportOW(Dimensions.END, link.sp.p);
						}else link.sp.p.sendMessage("§cPortail invalide !");
					}
				}
			}
		}
	}
}
