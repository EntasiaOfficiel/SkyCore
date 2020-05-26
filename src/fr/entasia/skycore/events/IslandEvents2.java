package fr.entasia.skycore.events;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.CooManager;
import fr.entasia.skycore.apis.ISPLink;
import fr.entasia.skycore.others.enums.Dimension;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class IslandEvents2 implements Listener {

	@EventHandler
	public void test(EntityTeleportEndGatewayEvent e) { // gl
		e.setCancelled(true);
	}
	@EventHandler
	public void test(PlayerTeleportEndGatewayEvent e) { // gl
		e.setCancelled(true);
	}

	@EventHandler
	public void dimension(PlayerPortalEvent e){ // gl
		Location loc = e.getFrom();
		Location loc2 = loc.clone().add(0, 1, 0);
		BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(loc));
		if(is!=null){
			ISPLink link = is.getMember(e.getPlayer().getUniqueId());
			if(link!=null){
				e.setCancelled(true);
				if (loc.getWorld()== Dimension.OVERWORLD.world) {
					if (loc.getBlock().getType() == Material.PORTAL||loc2.getBlock().getType()==Material.PORTAL) is.teleportNether(link.sp.p);
					else if (loc.getBlock().getType() == Material.ENDER_PORTAL||loc2.getBlock().getType()==Material.ENDER_PORTAL){
						is.teleportEnd(link.sp.p);
					} else link.sp.p.sendMessage("§cPortail invalide !");
				}else if (loc.getWorld()== Dimension.NETHER.world) {
					if (loc.getBlock().getType() == Material.PORTAL||loc2.getBlock().getType()==Material.PORTAL) is.teleportOverWord(link.sp.p);
					else link.sp.p.sendMessage("§cPortail invalide !");
				}else if (loc.getWorld()== Dimension.END.world) {
					if (loc.getBlock().getType() == Material.ENDER_PORTAL||loc2.getBlock().getType()==Material.ENDER_PORTAL){
						is.teleportOverWord(link.sp.p);
					} else link.sp.p.sendMessage("§cPortail invalide !");
				}
			}
		}
	}
}
