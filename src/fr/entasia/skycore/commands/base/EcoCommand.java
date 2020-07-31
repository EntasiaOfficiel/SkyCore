package fr.entasia.skycore.commands.base;

import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.SkyPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EcoCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender.hasPermission("skyblock.eco")){
			if(args.length >= 2){
				int money;
				try{
					money = Integer.parseInt(args[1]);
				}catch(NumberFormatException e){
					sender.sendMessage("§cLe nombre "+args[1]+" est invalide !");
					return true;
				}
				SkyPlayer sp;
				boolean tierce=false;
				if(args.length >= 3){
					sp = BaseAPI.getArgSP(sender, args[2], true);
					if(sp==null)return true;

					tierce = true;
				}else{
					if(sender instanceof Player) sp = BaseAPI.getSkyPlayer((Player)sender);
					else{
						sender.sendMessage("§cTu es la console ! Choisi un joueur en argument 3");
						return true;
					}
					sp = BaseAPI.getSkyPlayer((Player)sender);
				}
				if(sp==null) sender.sendMessage("§cUne erreur est survenue ! (Joueur invalide)");
				else{
					switch(args[0].toLowerCase()) {
						case "set":
							sp.setMoney(money);
							if (tierce)
								sender.sendMessage("§aTu as défini la monnaie de "+sp.name+" à "+money+" !");
							else
								sender.sendMessage("§aTu as défini ta monnaie à " + money + " !");
							break;
						case "give":
						case "add":
							sp.addMoney(money);
							if (tierce)
								sender.sendMessage("§aTu as ajouté " + money + " à la monnaie de "+sp.name+" ! Nouvelle valeur : " +sp.getMoney());
							else
								sender.sendMessage("§aTu as ajouté " + money + " à ta monnaie ! Nouvelle valeur : " + sp.getMoney());
							break;
						case "take":
							sp.withdrawMoney(money);
							if (tierce)
								sender.sendMessage("§aTu as retiré " + money + " de la monnaie de "+sp.name+" ! Nouvelle valeur : " + sp.getMoney());
							else
								sender.sendMessage("§aTu as retiré " + money + " de ta monnaie ! Nouvelle valeur : " + sp.getMoney());
						default:
							sender.sendMessage("§cAction à prendre sur la monnaie invalide !");
					}
				}
			}else sender.sendMessage("§cSyntaxe : /eco <set/give/take> <montant> [joueur]");
		}else sender.sendMessage("§cTu n'as pas accès à cette commande !");
		return true;
	}
}