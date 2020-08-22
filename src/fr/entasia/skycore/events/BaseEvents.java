package fr.entasia.skycore.events;

import fr.entasia.apis.utils.PlayerUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.objs.enums.Dimensions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BaseEvents implements Listener {


	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(InternalAPI.isFullyEnabled()){
			try{
				SkyPlayer sp = BaseAPI.getSkyPlayer(e.getPlayer().getUniqueId());
				if (sp==null) {
					sp = BaseAPI.registerSkyPlayer(e.getPlayer());
					Bukkit.broadcastMessage("§6Bienvenue à §e" + e.getPlayer().getDisplayName() + "§6 sur le Skyblock ! Souhaitons-lui la bienvenue !");
				}
				sp.p = e.getPlayer();
				for(ISPLink link : sp.getIslands()){
					link.is.tryLoad();
				}

				Utils.onlineSPCache.add(sp);

				if(!InternalAPI.enableIGSQL&&sp.p.hasPermission("errorlog")){
					sp.p.sendMessage("§cATTENTION : Les sauvegardes SQL sont désactivées ! Fait §4/isadmin sql enable§C pour les activer");
					sp.p.sendMessage("§cNON, ce n'est pas quelque chose de normal sur un serveur accessibles aux joueurs ! Préviens iTrooz_ si c'est le cas");
				}

				ISPLink link = sp.referentIsland(false);
				if(link==null)sp.p.teleport(Utils.spawn);
				else link.is.teleportHome(sp.p);

			}catch(Exception e2){
				e2.printStackTrace();
				e.getPlayer().kickPlayer("§c): Une erreur est survenue lors de la lecture de ton profil Skyblock ! Contacte un Membre du Staff");
			}
		}else e.getPlayer().kickPlayer("§cLe Post-Loading du plugin Skyblock n'est pas terminé ! Si tu ne peux pas te connecter dans une minute, contacte un Membre du Staff");
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Utils.onlineSPCache.removeIf(osp->osp.uuid.equals(e.getPlayer().getUniqueId()));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if (e.getDamager() instanceof Player) {
			e.setCancelled(true);
			return;
		}
		if (!Dimensions.isIslandWorld(p.getWorld())) return;
		if (e.getDamager() instanceof Firework) e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public static void onDamage(EntityDamageEvent e){
		if(e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			if(p.getLocation().getWorld()==Utils.spawnWorld){
				p.teleport(Utils.spawn);
				e.setCancelled(true);
				return;
			}
			if (e.getCause() == EntityDamageEvent.DamageCause.VOID){
				e.setCancelled(true);
				BaseIsland is = BaseAPI.getIsland(p.getLocation());
				if(is==null)p.teleport(Utils.spawn);
				else is.teleportHome(p);
				return;
			}
			if (e.getCause() == EntityDamageEvent.DamageCause.FALL) e.setDamage(e.getDamage() / 2);
			if (e.getFinalDamage() >= p.getHealth()) {
				e.setCancelled(true);
				p.sendMessage("§cTu es mort ! ):");
				for (PotionEffect pe : p.getActivePotionEffects()) {
					p.removePotionEffect(pe.getType());
				}

				SkyPlayer sp = BaseAPI.getOnlineSP(p.getUniqueId());
				assert sp != null;

				Location loc = Utils.spawn;
				if (Dimensions.isIslandWorld(p.getWorld())) {
					BaseIsland is = BaseAPI.getIsland(p.getLocation());
					if(is!=null)loc = is.getHome();
				}

				PlayerUtils.fakeKill(p);
				p.setNoDamageTicks(80); // c'est pas des ticks
				p.teleport(loc);

				new BukkitRunnable() {
					@Override
					public void run() {
						p.setFireTicks(0);
						p.setVelocity(new Vector(0, 0, 0));
					}
				}.runTask(Main.main);
			}
		}else if(e.getEntity() instanceof Snowman){
			if(e.getCause()==EntityDamageEvent.DamageCause.MELTING){
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e){ // pas sensé se produire mais bon
		e.setRespawnLocation(Utils.spawn);
	}

	@EventHandler
	public void initEvent(WorldInitEvent e){
		switch(e.getWorld().getName().toLowerCase()){
			case "iles":
				Dimensions.OVERWORLD.world = e.getWorld();
				break;
			case "iles_nether":
				Dimensions.NETHER.world = e.getWorld();
				break;
			case "iles_the_end":
				Dimensions.END.world = e.getWorld();
				break;
			default:
				return;
		}
		Main.main.getLogger().info("loaded "+e.getWorld().getName());
		for(Dimensions d : Dimensions.values()){
			if(d.world==null)return;
		}
		InternalAPI.onPostEnable();
	}



	@EventHandler
	public void a(EntityExplodeEvent e){
		e.blockList().clear();
	}

	@EventHandler
	public void a(BlockExplodeEvent e){
		e.blockList().clear();
	}

}
