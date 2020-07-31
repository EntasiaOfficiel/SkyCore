package fr.entasia.skycore.commands.manage;

import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.objs.CodePasser;
import fr.entasia.skycore.others.enums.Dimensions;
import fr.entasia.skycore.others.enums.MemberRank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IsAdminCommand implements CommandExecutor {

	private static void args(CommandSender sender){
		sender.sendMessage("§cArguments disponibles : ");
		sender.sendMessage("§4Joueurs :");
		sender.sendMessage("§c- infop");
		sender.sendMessage("§c- deletep");
		sender.sendMessage("§4ïles :");
		sender.sendMessage("§c- infois");
		sender.sendMessage("§c- deleteis");
		sender.sendMessage("§c- join");
		sender.sendMessage("§c- quit");
		sender.sendMessage("§c- setowner");
		sender.sendMessage("§4Autres :");
		sender.sendMessage("§c- help");
	}


	private static BaseIsland getIS(CommandSender sender, String[] args) {
		if (args.length == 1) sender.sendMessage("§cMet un ID d'île !");
		else {
			ISID isid = ISID.parse(args[1]);
			if (isid == null) sender.sendMessage("§cID d'ile invalide !");
			else {
				BaseIsland is = BaseAPI.getIsland(isid);
				if (is == null) sender.sendMessage("§cIle non existante !");
				else return is;
			}
		}
		return null;
	}

	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		if(sender.hasPermission("restricted.isadmin")){

			Player p = ((Player)sender);
			SkyPlayer sp = BaseAPI.getSkyPlayer(p.getUniqueId());
			if (args.length==0)p.sendMessage("§cFait /isadmin help pour voir la liste des commandes !");
			else{
				args[0] = args[0].toLowerCase();
				switch (args[0]) {

					// INFOS

					case "infop": {
						if(args.length==1)p.sendMessage("§cMet un pseudo/UUID !");
						else {
							SkyPlayer target = BaseAPI.getArgSP(p, args[1], false);
							if(target!=null){
								p.sendMessage("Joueur : " + target.p.getName());
								p.sendMessage("îles :");
								for(ISPLink ll : target.getIslands()){
									p.sendMessage("- île "+ll.is.isid.str()+" ("+ll.getRank().color+ll.getRank().name+"§f)");
								}
								p.sendMessage("Monnaie : " + target.getMoney());
							}
						}
						break;
					}
					case "infois": {
						ISID isid;
						if (args.length == 1) {
							if (Dimensions.isIslandWorld(p.getWorld())) {
								isid = CooManager.getIslandID(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
							} else{
								p.sendMessage("§cTu n'es pas dans un monde Skyblock !");
								return true;
							}
						} else isid = ISID.parse(args[1]);
						if (isid == null) p.sendMessage("§cID d'ile invalide !");
						else {
							BaseIsland is = BaseAPI.getIsland(isid);
							if (is == null) p.sendMessage("§cIle non existante !");
							else {
								p.sendMessage("§8Global");
								p.sendMessage("§7ID : §b"+isid.str());
								ISPLink link = is.getOwner();
								p.sendMessage("§7Owner UUID : §b"+link.sp.uuid);
								if(link.sp.p!=null)p.sendMessage("§7Owner Name : §b"+link.sp.p.getName());
								p.sendMessage("§7Membres :");
								for(ISPLink ll : link.is.getSortedMembers()){
									p.sendMessage("§8- §b"+ll.getName());
								}
								p.sendMessage("§7Mineurs : §b"+is.autominers.size());
								p.sendMessage("§7Niveau : §b" + is.getLevel());
								p.sendMessage("§8Dimensions :");
								p.sendMessage("§7Nether : §b"+is.hasDimension(Dimensions.NETHER));
								p.sendMessage("§7End : §b"+is.hasDimension(Dimensions.END));
							}
						}
						break;
					}

					// DELETE

					case "deletep":{
						if(args.length==1)p.sendMessage("§cMet un pseudo/UUID !");
						else{
							SkyPlayer target = BaseAPI.getArgSP(p, args[1], false);
							if(target!=null){
								if(BaseAPI.deleteSkyPlayer(target))p.sendMessage("§cJoueur supprimé avec succès !");
								else p.sendMessage("§4Erreur lors de la suppression du joueur !");
							}
						}
						break;
					}

					case "deleteis":{
						if(args.length==1)p.sendMessage("§cMet un ID d'île !");
						else {
							ISID isid = ISID.parse(args[1]);
							if (isid == null) p.sendMessage("§cID d'ile invalide !");
							else{
								BaseIsland is = BaseAPI.getIsland(isid);
								if (is == null) p.sendMessage("§cIle non existante !");
								else {
									BaseAPI.deleteIsland(is, new CodePasser.Bool() {
										@Override
										public void run(boolean err) {
											if(err)p.sendMessage("§cUne erreur s'est produite lors de la suppression de l'île !");
											else p.sendMessage("§cîle supprimé avec succès !");
										}
									});
								}
							}
						}
						break;
					}

					// TEAM UTILS

					case "setrange":{
						BaseIsland is = getIS(sender, args);
						if(is!=null){
							byte range;
							try{
								if(args.length==2)throw new NumberFormatException();
								range = Byte.parseByte(args[2]);
								if(range<0||range>3)throw new NumberFormatException();
							}catch(NumberFormatException ignore){
								p.sendMessage("§cMet un nombre entre 0 et 3 !");
								return true;
							}
							is.setExtension(range);
							p.sendMessage("§aNouvelle extension de l'île définie à "+range+" !");
						}
					}
					case "join":{
					}
					case "kick":{
					}
					case "setowner":{
						BaseIsland is = getIS(sender, args);
						if(is!=null){
							if(args.length==2)p.sendMessage("§cMet un joueur !");
							else{
								SkyPlayer target = BaseAPI.getArgSP(sender, args[1], false);
								if(target!=null){
									ISPLink newLink = target.getIsland(is.isid);
									switch(args[0]){
										case "join":{
											if(newLink==null){
												if(is.addMember(target, MemberRank.RECRUE))p.sendMessage("§aSuccès !");
												else p.sendMessage("§cUne erreur est survenue !");
											}else p.sendMessage("§cCe joueur est déja membre sur cette île !");
											break;
										}
										case "kick":{
											if(newLink==null)p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
											else{
												if(is.removeMember(newLink))p.sendMessage("§aSuccès !");
												else p.sendMessage("§cUne erreur est survenue !");
											}
											break;
										}
										case "setowner":{
											if(newLink==null)p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
											else {
												if (is.reRankMember(newLink, MemberRank.CHEF)) p.sendMessage("§aSuccès !");
												else p.sendMessage("§cUne erreur est survenue !");
											}
											break;
										}
									}
								}
							}
						}
						break;
					}


					// AUTRES
					case "help": {
						args(sender);

						break;
					}
					default:{
						p.sendMessage("§cL'argument "+args[0]+" n'existe pas !");
						args(sender);
						break;
					}
				}
			}
		}else sender.sendMessage("§cTu n'as pas la permission d'utiliser cette commande !");
		return true;
	}
}