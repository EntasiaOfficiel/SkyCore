package fr.entasia.skycore.events;

import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.CooManager;
import fr.entasia.skycore.apis.ISPLink;
import fr.entasia.skycore.others.enums.Dimensions;
import fr.entasia.skycore.others.enums.MemberRank;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.EnumSet;
import java.util.Set;

public class IslandEvents implements Listener {

	private static final Set<Material> containers = EnumSet.of(
			Material.CHEST,
			Material.TRAPPED_CHEST,

			Material.DROPPER,
			Material.HOPPER,
			Material.DISPENSER,

			Material.FURNACE,
			Material.BURNING_FURNACE,

			Material.BREWING_STAND);


	@EventHandler
	public void interact(PlayerInteractEvent e){
		Player p = e.getPlayer();
		if(Dimensions.isIslandWorld(p.getWorld())&&e.hasBlock()){
			if(p.hasPermission("skyblock.isprotect.bypass")&&p.getGameMode()==GameMode.CREATIVE)return;
			Block b = e.getClickedBlock();
			BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(b.getLocation()));
			if(e.getAction()==Action.RIGHT_CLICK_BLOCK){
				if(containers.contains(b.getType())){
					if(is==null)e.setCancelled(true);
					else{
						ISPLink link = is.getMember(p.getUniqueId());
						if(link==null) {
							p.sendMessage("§cTu ne peux pas ouvrir les containers sur cette ile !");
							e.setCancelled(true);
						}else if(link.getRank() == MemberRank.RECRUE) {
							p.sendMessage("§cTu es seulement une recrue sur cette ile ! Tu ne peux pas interagir avec les containers");
							e.setCancelled(true);
						}
					}
				}
			}
		} // on bloque pas les interactions dans les autres mondes (pour le moment ?)
	}

	public static boolean isBlockDenied(Player p, Block b){
		if(Utils.masterEditors.contains(p))return false;
		if(Dimensions.isIslandWorld(p.getWorld())) {
			BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(b.getLocation()));
			if (p.hasPermission("skyblock.isprotect.bypass") && p.getGameMode() == GameMode.CREATIVE) return false;
			if (is != null) {
				ISPLink link = is.getMember(p.getUniqueId());
				if (link == null) {
					p.sendMessage("§cTu ne peux pas casser de blocks sur cette ile !");
					;
				} else {
					if (is.hasDimension(Dimensions.getDimension(p.getWorld()))) {
						int m = is.isid.distanceFromIS(b.getLocation());
						if ((is.getExtension() + 1) * 50 <= m) {
							p.sendMessage("§cL'extension de ton ile n'est pas suffisante !");
						} else if (link.getRank() == MemberRank.RECRUE && containers.contains(b.getType())) {
							p.sendMessage("§cTu es seulement une recrue sur cette ile ! Tu ne peux pas intéragir avec les containers");
						} else return false;
					} else
						p.sendMessage("§Ton île n'a pas encore débloqué cette dimension ! Utilise un portail pour la débloquer");
				}
			}
		}
		return true;
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent e){
		e.setCancelled(isBlockDenied(e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent e){
		e.setCancelled(isBlockDenied(e.getPlayer(), e.getBlock()));
	}


	private static String checkIs(BaseIsland is, Player p){ // temporaire ?
		if(is.getMember(p.getUniqueId())==null){
			return "l'ile de §6"+is.getOwner().sp.name+"§f";
		}else{
			return "ton ile";
		}
	}

	@EventHandler
	public void onAnyMovement(PlayerMoveEvent e){
		Player p = e.getPlayer();
		if(Dimensions.isIslandWorld(p.getWorld())){
			BaseIsland fr = BaseAPI.getIsland(CooManager.getIslandID(e.getFrom()));
			BaseIsland to = BaseAPI.getIsland(CooManager.getIslandID(e.getTo()));
			if(fr==to){ // on est sur la même île
				if(fr!=null){
					int ext = (fr.getExtension()+1)*50;
					int m1 = fr.isid.distanceFromIS(e.getFrom());
					int m2 = fr.isid.distanceFromIS(e.getTo());
					if(m1<ext){
						if(m2>=ext){
							e.getPlayer().sendActionBar("§fTu es sorti de la zone de "+ checkIs(fr, p)+" !");
						}
					}else if (m2<ext){
						e.getPlayer().sendActionBar("§fTu es rentré dans la zone de "+ checkIs(fr, e.getPlayer())+" !");
					}
				}
			}else{
				if (to == null) {
					e.getPlayer().sendActionBar("§fTu es sorti de "+ checkIs(fr, p)+" !");
				} else {
					e.getPlayer().sendActionBar("§fTu es rentré sur "+ checkIs(to, p)+" !");
				}
			}
		}
	}
}
