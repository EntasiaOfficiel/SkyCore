package fr.entasia.skycore.events;

import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.ISPLink;
import fr.entasia.skycore.apis.SkyPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static org.bukkit.event.EventPriority.HIGH;

public class ChatEvents implements Listener {

	@EventHandler(priority = HIGH)
	public void onChat(AsyncPlayerChatEvent e){
		SkyPlayer sp  = BaseAPI.getOnlineSP(e.getPlayer().getUniqueId());
		assert sp != null;
		ISPLink link = sp.getDefaultIS();
		if(link!=null){
			if(sp.islandChat){
				e.setCancelled(true);
				link.is.islandChat(link, String.join(" ", e.getMessage()));
			}else{
				e.setFormat("[" + link.is.getLevel() + "] " + e.getFormat());
				// SUITE EVENT ICI
			}
		}

	}


}
