package fr.entasia.skycore.events;

import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.others.enums.Dimension;
import org.bukkit.Bukkit;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;

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

				Utils.onlineSPCache.add(sp);

				if(!InternalAPI.enableIGSQL&&sp.p.hasPermission("errorlog")){
					sp.p.sendMessage("§cATTENTION : Les sauvegardes SQL sont désactivées ! Fait §4/isadmin sql enable§C pour les activer");
					sp.p.sendMessage("§cNON, ce n'est pas quelque chose de normal sur un serveur accessibles aux joueurs ! Préviens iTrooz_ si c'est le cas");
				}

				ISPLink link = sp.referentIsland(false);
				if(link==null)sp.p.teleport(Utils.spawn);
				else sp.p.teleport(link.is.getHome());

			}catch(Exception e2){
				e2.printStackTrace();
				e.getPlayer().kickPlayer("§c): Une erreur est survenue lors de la lecture de ton profil Skyblock ! Contacte un membre du Staff");
			}
		}else e.getPlayer().kickPlayer("§cLe Post-Loading du plugin Skyblock n'est pas terminé ! Si tu ne peux pas te connecter dans une minute, contacte un membre du Staff");
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		SkyPlayer sp = BaseAPI.getOnlineSP(e.getPlayer().getUniqueId());
		assert sp != null;
		Utils.onlineSPCache.removeIf(aaa->aaa.uuid.equals(sp.uuid));
	}

	@EventHandler
	public static void onDamage(EntityDamageByEntityEvent e){
		if(!(e.getEntity() instanceof Player))return;
		Player p = (Player)e.getEntity();
		if(!Dimension.isIslandWorld(p.getWorld()))return;
		if(e.getDamager() instanceof Firework) e.setCancelled(true);
		else if(e.getDamager() instanceof Player)e.setCancelled(true);
		else{
			if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
				e.setDamage(e.getDamage() / 2);
			if (e.getFinalDamage() >= p.getHealth()) {
				e.setCancelled(true);
				p.sendMessage("§c§kn§cTu es mort !§kn");
				for(PotionEffect pe : p.getActivePotionEffects()){
					p.removePotionEffect(pe.getType());
				}
				p.setMaxHealth(20);
				p.setHealth(20);
				p.setFoodLevel(20);


				SkyPlayer sp = BaseAPI.getOnlineSP(p.getUniqueId());
				assert sp != null;

				BaseIsland is = null;
				if (Dimension.isIslandWorld(p.getWorld())) {
					is = BaseAPI.getIsland(CooManager.getIslandID(p.getLocation()));
				}
				if (is == null) {
					ArrayList<ISPLink> a = sp.getIslands();
					if (a.size() == 1) is = a.get(0).is;
					else {
						is = sp.getDefaultIS().is;
						if (is == null) {
							p.teleport(Utils.spawn);
							return;
						}
					}
				}
				p.teleport(is.getHome());
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
				Dimension.OVERWORLD.world = e.getWorld();
				break;
			case "iles_nether":
				Dimension.NETHER.world = e.getWorld();
				break;
			case "iles_the_end":
				Dimension.END.world = e.getWorld();
				break;
			default:
				return;
		}
		for(Dimension d : Dimension.values()){
			if(d.world==null)return;
		}
		InternalAPI.onPostEnable();
	}


	@EventHandler
	public void a(EntityExplodeEvent e){
		e.get
	}
}
