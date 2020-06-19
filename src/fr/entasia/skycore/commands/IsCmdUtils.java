package fr.entasia.skycore.commands;

import fr.entasia.apis.ChatComponent;
import fr.entasia.skycore.apis.BaseIsland;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class IsCmdUtils {

	public static void sendInviteMsg(CommandSender sender, BaseIsland is){
		ChatComponent accept = new ChatComponent("§2[§aAccepter§2]");
		accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept "+is.isid.str()));
		accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponent.create("§aClique pour accepter l'invitation !")));

		ChatComponent deny = new ChatComponent("§4[§cRefuser§4]");
		deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is deny "+is.isid.str()));
		deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponent.create("§cClique pour refuser l'invitation !")));

		sender.sendMessage(accept.append("   ").append(deny).create());
	}

	public static class WaitConfirm extends BukkitRunnable {

		public Player p;
		public HashMap<Player, ConfirmObj> list;
		public WaitConfirm(Player p, HashMap<Player, ConfirmObj> list){
			this.p = p;
			this.list = list;
		}

		@Override
		public void run() {
			p.sendMessage("§cTemps de confirmation expiré !");
			list.remove(p);
		}
	}

	public static class ConfirmObj{
		public long when;
		public BaseIsland is;
		public BukkitTask task;

		public ConfirmObj(long when, BaseIsland is){
			this.when = when;
			this.is = is;
		}
	}

	public static HashMap<Player, ConfirmObj> confirmDelete = new HashMap<>();
	public static HashMap<Player, ConfirmObj> confirmPassOwner = new HashMap<>();

}
