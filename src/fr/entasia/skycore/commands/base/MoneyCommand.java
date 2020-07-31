package fr.entasia.skycore.commands.base;

import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.SkyPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		SkyPlayer sp;
		if(args.length==0){
			if(sender instanceof Player){
				sp = BaseAPI.getSkyPlayer(((Player) sender));
				sender.sendMessage("§aTa monnaie : §2"+sp.getMoney());
			}else sender.sendMessage("§cTu es la console ! Met un nom de joueur");
		}else{
			sp = BaseAPI.getArgSP(sender, args[0], false);
			if(sp==null)sender.sendMessage("§cCe joueur n'existe pas !");
			else sender.sendMessage("§aMonnaie de "+sp.name+" : "+sp.getMoney());
		}
		return true;
	}
}
