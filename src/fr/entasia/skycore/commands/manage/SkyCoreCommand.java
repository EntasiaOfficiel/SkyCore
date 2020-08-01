package fr.entasia.skycore.commands.manage;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.InternalAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkyCoreCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		if(sender.hasPermission("plugin.skycore")){
			Player p = (Player)sender;
			if(args.length==0){
				p.sendMessage("§cMet un argument !");
				showArgs(sender);
			}else{
				if(args[0].equalsIgnoreCase("reload")) {
					try {
						Main.loadConfigs();
						p.sendMessage("§aConfiguration rechargée avec succès !");
					} catch (Exception e) {
						e.printStackTrace();
						p.sendMessage("§cConfiguration rechargée avec erreur ! ( voir console )");
					}

				}else if(args[0].equalsIgnoreCase("sql")){
					if(args.length==1)p.sendMessage("§cArguments : enable/disable");
					else if(args[1].equalsIgnoreCase("enable")){
						InternalAPI.enableIGSQL = true;
						p.sendMessage("§aSauvegardes SQL activées !");
					}else if(args[1].equalsIgnoreCase("disable")){
						InternalAPI.enableIGSQL = false;
						p.sendMessage("§cSauvegardes SQL désactivées !");

					}else p.sendMessage("§cArgument invalide !");
				}else if(args[0].equalsIgnoreCase("masteredit")){
					if(Utils.masterEditors.remove(p)) {
						p.sendMessage("§6Master Editor §cDésactivé §6!");
					}else{
						Utils.masterEditors.add(p);
						p.sendMessage("§6Master Editor §aActivé §6!");
					}

				}else {
					p.sendMessage("§cArgument invalide ! Arguments disponibles :");
					showArgs(sender);
				}
			}
		}else sender.sendMessage("§cTu n'as pas accès à cette commande !");
		return true;
	}

	private static void showArgs(CommandSender sender){
		sender.sendMessage("§c- reload");
		sender.sendMessage("§c- sql");
		sender.sendMessage("§c- masteredit");
	}
}
