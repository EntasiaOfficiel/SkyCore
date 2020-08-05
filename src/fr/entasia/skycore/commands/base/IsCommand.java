package fr.entasia.skycore.commands.base;

import fr.entasia.apis.other.ChatComponent;
import fr.entasia.apis.other.CodePasser;
import fr.entasia.apis.utils.TextUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.invs.IsMenus;
import fr.entasia.skycore.objs.enums.Dimensions;
import fr.entasia.skycore.objs.enums.MemberRank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

import static fr.entasia.skycore.commands.base.IsCmdUtils.*;
//import static fr.entasia.skycore.commands.base.IsCmdUtils.*;

public class IsCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		Player p = ((Player)sender);
		SkyPlayer sp = BaseAPI.getOnlineSP(p.getUniqueId());
		if(sp==null){
			p.sendMessage("§cTon profil est mal chargé ! Contacte un Membre du Staff");
			return true;
		}
		ISPLink link = sp.referentIsland(true);
		if (args.length == 0) {
			if (sp.getIslands().size()==0) IsMenus.startIslandChooseOpen(sp);
			else if(link==null) {
				p.sendMessage("§cTu dois d'abord choisir une île préférée pouvoir choisir ces options ! (On ne sait pas de laquelle tu parles !)");
				IsMenus.islandsListOpen(sp, true);
			}else IsMenus.baseIslandOpen(link);
			return true;
		}
		args[0] = args[0].toLowerCase();
		switch (args[0]) {
			case "dis":
			case "list":{
				IsMenus.islandsListOpen(sp, false);
				break;
			}

			case "invites":{
				ArrayList<BaseIsland> l1 = sp.getInvites();
				if(l1.size()!=0){
					p.sendMessage("§aTes invitations : ");
					for(BaseIsland is : l1){
						if(is.getName()==null) p.sendMessage("§e- §aîle "+is.isid.str()+" (Propriétaire : "+is.getOwner().sp.name+")");
						else p.sendMessage("§e- §aîle "+is.getName()+" (ID:"+is.isid.str()+") (Propriétaire : "+is.getOwner().sp.name+")");
						sendInviteMsg(p, is);
						p.sendMessage(" ");
					}
					p.sendMessage(" ");
				}

				ArrayList<SkyPlayer> l2=null;
				for(ISPLink ll : sp.getIslands()){
					l2 = ll.is.getInvites();
					if(l2.size()!=0){
						if(ll.is.getName()==null) p.sendMessage("§e- §aîle "+ll.is.isid.str());
						else p.sendMessage("§e- §aîle "+ll.is.getName()+" (ID:"+ll.is.isid.str()+")");
						ChatComponent uninvite = new ChatComponent("§4[§cAnnuler§4]");
						uninvite.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponent.create("§cClique pour annuler l'invitation !")));
						for(SkyPlayer lsp : l2){
							uninvite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is uninvite "+lsp.uuid));
							sender.sendMessage(ChatComponent.create(new ChatComponent("§e- §2"+lsp.name+"  "), uninvite));
							p.sendMessage(" ");
						}
						p.sendMessage(" ");
					}
				}
				if(l1.size()==0&&l2==null)p.sendMessage("§cTu n'as aucune invitation à gérer !");
				break;
			}

			case "accept": {
				if (args.length == 1) p.sendMessage("§cMet un ID d'île !");
				else {
					ISID isid = ISID.parse(args[1]);
					if (isid == null) p.sendMessage("§cID d'île invalide !");
					else {
						ArrayList<BaseIsland> list = sp.getInvites();
						for (BaseIsland is : list) {
							if (is.isid.equals(isid)) {
								if (is.cancelInvite(sp) && is.addMember(sp, MemberRank.RECRUE)) {
									p.sendMessage("§aInvitation acceptée ! Bienvenue, §dRecrue§a !");
									p.teleport(is.getHome());
								} else p.sendMessage("§cUne erreur s'est produite lors de l'acceptation de l'invitation !");
								return true;
							}
						}
						p.sendMessage("§cTu n'as pas recu d'invitation de la part de cette île !");
					}
				}
				break;
			}

			case "deny":{
				if(args.length==1)p.sendMessage("§cMet un ID d'île !");
				else{
					ISID isid = ISID.parse(args[1]);
					if(isid==null)p.sendMessage("§cID d'île invalide !");
					else{
						BaseIsland is = sp.getInvite(isid);
						if(is==null) p.sendMessage("§cTu n'as pas recu d'invitation de la part de cette île !");
						else{
							if(is.cancelInvite(sp)){
								p.sendMessage("§cTu as refusé l'invitation de l'île "+is.getNameOrID()+" !");
								is.sendTeamMsg("§3"+sp.p.getDisplayName()+" §cà refusé l'invitation !");
							}else p.sendMessage("§cUne erreur s'est produite lors du refus de l'invitation !");
						}
					}
				}
				break;
			}
			default:{
				if(link==null){
					p.sendMessage("§cTu dois d'abord avoir au moins une île avant de pouvoir choisir ces options !");
					IsMenus.startIslandChooseOpen(sp);
					return true;
				}
				switch (args[0]) {
					case "go":
					case "h":
					case "home": {
						if (args.length > 1) {
							try {
								int index = Integer.parseInt(args[1]);
								if (index <= 0) p.sendMessage("§cCe numéro d'île est invalide ! (0/Négatif)");
								else {
									ArrayList<ISPLink> list = link.sp.getIslands();
									if (list.size() < index)
										p.sendMessage("§cAucune île ne correspond à ce numéro d'île !");
									else {
										ISPLink newLink = list.get(index - 1);
										p.setFallDistance(0);
										p.teleport(newLink.is.getHome());
										p.sendMessage("§6Tu as été téléporté à ton île n° " + index + " !");
									}
								}
							} catch (NumberFormatException e) {
								p.sendMessage("§cCe numéro d'île est invalide !");
							}
						} else {
							p.setFallDistance(0);
							p.teleport(link.is.getHome());
							p.sendMessage("§6Tu as été téléporté à ton île !");
						}
						break;
					}
					case "team": {
						IsMenus.manageTeamOpen(link);
						break;
					}
					case "sethome": {
						if(link.getRank()==MemberRank.RECRUE)p.sendMessage("§cTu es une recrue, tu ne peux pas redéfinir le spawn de l'île !");
						else{
							link.is.setHome(p.getLocation());
							p.sendMessage("§aLe spawn de l'île à été redéfini avec succès !");
						}
						break;
					}
					case "c":
					case "chat": {
						if (args.length == 1) {
							if (link.sp.islandChat) {
								link.sp.islandChat = false;
								p.sendMessage("§cChat d'île désactivé !");
							} else {
								link.sp.islandChat = true;
								p.sendMessage("§aChat d'île activé !");
							}
						} else {
							link.is.islandChat(link, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
						}
						break;
					}

					case "calc":
					case "level":
					case "lvl": {

						int a = link.is.updateLvl(new CodePasser.Arg<Integer>() {
							@Override
							public void run(Integer rem) {
								p.sendMessage("§aNiveau de l'île : " + link.is.getLevel());
//								p.sendMessage("§aPoints demandés pour le niveau suivant : "+rem);
							}
						});
						if (a == 0) p.sendMessage("§aCalcul du niveau de l'île en cours...");
						else
							p.sendMessage("§cTu dois encore attendre " + TextUtils.secondsToTime(a) + " avant de recalculer le niveau de l'île !");

						break;
					}

					case "leave":
					case "quit": {
						if(link.getRank()==MemberRank.CHEF){
							p.sendMessage("§cTu es le chef de cette île, tu ne peux pas la quitter !");
							p.sendMessage("§cUtilise /is setowner pour transférer la propriété de l'île");
						}else{
							String name = link.getName();
							if (link.is.removeMember(link)) {
								link.is.sendTeamMsg(name + "§e à quitté l'île !");
								p.sendMessage("§cTu as quitté l'île !");
							} else p.sendMessage("§cUne erreur s'est produite !");
							break;
						}
					}

					case "ban":
					case "unban":{
						SkyPlayer target = IsCmdUtils.teamCheck(link, args);
						if (target == null) return true;
						if (args[0].equals("ban")) {
							if (link.is.addBanned(target)) {
								p.sendMessage("§cTu as banni " + target.name + " !");
								link.is.sendTeamMsg(MemberRank.DEFAULT.getName() + "§3 " + target.name + "§c à été bannu de l'île par " + link.getName() + "§c !");
							} else {
								p.sendMessage("§cCe joueur est déja banni !");
							}
						} else if (args[0].equals("unban")) {
							if (link.is.removeBanned(target)) {
								p.sendMessage("§cTu as débanni " + target.name + " !");
								link.is.sendTeamMsg(MemberRank.DEFAULT.getName() + "§3 " + target.name + "§e à été débanni de l'île par " + link.getName() + "§e !");
							} else {
								p.sendMessage("§cCe joueur n'est pas banni !");
							}
						}
						break;
					}

					case "invite":
					case "uninvite": {
						SkyPlayer target = IsCmdUtils.teamCheck(link, args);
						if (target == null) return true;
						ISPLink targetLink = link.is.getMember(target.uuid);
						if (args[0].equals("invite")) {
							if (targetLink == null) {
								if (link.is.invitePlayer(target)) {
									link.is.sendTeamMsg(MemberRank.DEFAULT.getName() + "§3 " + target.name + "§e à été invité sur l'île par " + link.getName() + "§e !");
									if (target.p != null) {
										target.p.sendMessage("§eTu as été invité sur l'île " + link.is.getNameOrID() + " par " + link.sp.name + " !");
										sendInviteMsg(target.p, link.is);
										target.p.sendMessage("§eTu peux à tout moment regarder tes invitations avec la commande §6/is invites");
									}
								} else p.sendMessage("§cCe joueur à déja été invité !");
							} else p.sendMessage("§cCe joueur est déja membre sur cette île !");
						} else if (args[0].equals("uninvite")) {
							if (targetLink == null) {
								if (link.is.cancelInvite(target)) {
									link.is.sendTeamMsg("§3L'invitation de " + target.name + "§e à été annulée par " + link.getName() + "§e !");
									if (target.p != null)
										target.p.sendMessage("§cL'invitation de l'île §4" + link.is.getNameOrID() + "§c à été annulée !");
								} else p.sendMessage("§cCe joueur n'est pas invité !");
							} else p.sendMessage("§cCe joueur est un membre de l'île ! Utilise §4/is kick");
						}
						break;
					}

					case "kick":
					case "demote":
					case "promote": {
						SkyPlayer target = IsCmdUtils.teamCheck(link, args);
						if (target == null) return true;
						ISPLink targetLink = link.is.getMember(target.uuid);
						if (targetLink == null) {
							p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
							return true;
						}
						switch (args[0]) {
							case "kick": {
								if (targetLink.getRank().id < link.getRank().id) {
									String n = targetLink.getName();
									if (link.is.removeMember(targetLink)) {
										link.is.sendTeamMsg("§7" + n + "§e à été expulsé de l'île par " + link.getName() + "§e !");
										if (target.isOnline()) {
											target.p.sendMessage("§cTu as été exclu de l'île par §3" + link.sp.name + "§c !");
											target.p.teleport(Utils.spawn);
										}
									} else p.sendMessage("§cUne erreur s'est produite !");
								} else p.sendMessage("§cCette personne est trop haut gradée !");
								break;
							}

							case "promote": {
								if (targetLink.getRank().id + 1 < link.getRank().id) {
									MemberRank nrank = MemberRank.getType(targetLink.getRank().id + 1);
									if (targetLink.setRank(nrank))
										link.is.sendTeamMsg(targetLink.getName() + "§e à été promu par " + link.getName() + "§e !");
									else p.sendMessage("§cUne erreur s'est produite !");
								} else {
									p.sendMessage("§cCette personne est trop haut gradée !");
								}
								break;
							}

							case "demote": {
								if (targetLink.getRank().id < link.getRank().id) {
									if (targetLink.getRank().id <= MemberRank.RECRUE.id)
										p.sendMessage("§cCette personne à déjà le rôle minimum ! Utilise §4/is kick§c pour l'exclure ");
									else {
										MemberRank nrank = MemberRank.getType(targetLink.getRank().id - 1);
										if (targetLink.setRank(nrank))
											link.is.sendTeamMsg(targetLink.getName() + "§e à été demote par " + link.getName() + "§e !");
										else p.sendMessage("§cUne erreur s'est produite !");
									}
								} else p.sendMessage("§cCette personne est trop haut gradée !");
								break;
							}
						}
						break;
					}


					case "deposit":
					case "withdraw":{
						if(args.length==1)p.sendMessage("§cMet un chiffre !");
						else{
							try{
								int n = Integer.parseInt(args[1]);
								if(args[0].equals("withdraw")){
									if(link.is.withdrawBank(n)){
										sp.addMoney(n);
										p.sendMessage("§aTu as retiré §2"+n+"§a$ de la banque d'île !");
									}else{
										p.sendMessage("§cIl n'y a pas assez d'argent dans la banque d'île !");
									}
								}else{
									if(sp.withdrawMoney(n)){
										link.is.addBank(n);
										p.sendMessage("§aTu as ajouté §2"+n+"§a$ à la banque d'île !");
									}else p.sendMessage("§cTu n'as pas assez d'argent !");

								}

							}catch(NumberFormatException ignore){
								p.sendMessage("§cLe chiffre §4"+args[1]+"§c est invalide !");
							}
						}
						break;
					}
					case "bank":
					case "money": {
						p.sendMessage("§eValeur de la banque d'île actuellement : §6"+link.is.getBank()+"§e$");
						break;
					}

					case "setowner": {
						if (args.length < 2) p.sendMessage("§cMet un joueur en argument !");
						else {
							if (link.sp.p.getWorld() == Dimensions.OVERWORLD.world) {
								BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(link.sp.p.getLocation()));
								if (is == null) p.sendMessage("§cTu n'es sur aucune île !");
								else {
									if (is.getOwner().sp.equals(link.sp)) {
										ConfirmObj co = confirmPassOwner.get(p);
										if (args.length == 2 && args[1].equals("confirm")) {
											if (co == null || (System.currentTimeMillis() - co.when > 10000)){
												p.sendMessage("§cLe temps de confirmation est écoulé !");
											}else {
												co.task.cancel();
												confirmPassOwner.remove(p);
												if (co.is.isid.equals(is.isid)) {
													SkyPlayer target = InternalAPI.getArgSP(link.sp.p, args[1], true);
													if (target != null) {
														if (target.equals(link.sp))
															p.sendMessage("§cCe joueur est.. toi-même ?");
														else {
															ISPLink newLink = link.sp.getIsland(is.isid);
															if (newLink == null)
																p.sendMessage("§cCe joueur n'est plus membre sur cette île !");
															else {
																is.sendTeamMsg("§3Passage du chef sur cette île à §c" + link.sp.p + " §3!");
																newLink.setRank(MemberRank.CHEF);
															}
														}
													}
												} else{
													p.sendMessage("§cL'île sur laquelle tu es n'est pas la même que celle ou tu as fait la première commande ! Annulation");
												}
											}
										} else {
											if (co == null) {
												SkyPlayer target = InternalAPI.getArgSP(p, args[1], true);
												if (target != null) {
													if (target.equals(link.sp))
														p.sendMessage("§cCe joueur est.. toi-même ?");
													else if (link.sp.getIsland(is.isid) == null)
														p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
													else {
														p.sendMessage("§cVeut-tu passer " + target.name + " propriétaire de cette île ? " + is.isid.str());
														p.sendMessage("§cTape la commande §4/" + command.getName() + " setowner " + args[1] + " confirm§c dans les 15 secondes pour confirmer.");
														p.sendMessage("§cATTENTION : Tu ne sera plus le chef de cette île, tu deviendra Adjoint !");
														co = new ConfirmObj(System.currentTimeMillis(), is);
														co.task = new WaitConfirm(p, confirmPassOwner).runTaskLaterAsynchronously(Main.main, 300); // 15*20 = 300 ticks
														confirmPassOwner.put(p, co);
													}
												}
											} else {
												int time = (int) (15 - (Math.ceil(System.currentTimeMillis() - co.when) / 1000f));
												p.sendMessage("§cTape la commande §4" + command.getName() + " setowner " + args[1] + " confirm§c dans les " + time +
														" secondes pour confirmer le changement de propriétaire de l'île " + is.isid.str());
											}
										}
									} else p.sendMessage("§cTu n'es pas le propriétaire de cette île !");
								}
							} else p.sendMessage("§cTu n'es pas dans l'overworld des îles !");
						}
						break;
					}


					case "delete": {
						if (link.sp.p.getWorld() == Dimensions.OVERWORLD.world) {
							BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(link.sp.p.getLocation()));
							if (is == null) p.sendMessage("§cTu n'es sur aucune île !");
							else {
								if (is.getOwner().sp.equals(link.sp)) {
									ConfirmObj co = confirmDelete.get(p);
									if (args.length == 2 && args[1].equals("confirm")) {
										if (co == null || (System.currentTimeMillis() - co.when > 10000))
											p.sendMessage("§cLe temps de confirmation est écoulé !");
										else {
											co.task.cancel();
											confirmDelete.remove(p);
											if (co.is.isid.equals(is.isid)) {
												p.sendMessage("§cSuppression de l'île " + is.isid.x + ";" + is.isid.z + " en cours...");
												BaseAPI.deleteIsland(is, new CodePasser.Arg<Boolean>() {
													@Override
													public void run(Boolean err) {
														if(err)p.sendMessage("§cîle supprimée avec succès !");
														else p.sendMessage("§cUne erreur est survenue !");
													}
												});
											} else p.sendMessage("§cL'île sur laquelle tu es n'est pas la même que celle ou tu as fait la première commande ! Annulation");
										}
									} else {
										if (co == null) {
											p.sendMessage("§cVeut tu supprimer cette île ? " + is.isid.str());
											p.sendMessage("§cTape la commande §4/" + command.getName() + " delete confirm§c dans les 15 secondes pour confirmer.");
											p.sendMessage("§cATTENTION : La suppression est instantanée et sans espoir de retour !");
											co = new ConfirmObj(System.currentTimeMillis(), is);
											confirmDelete.put(p, co);
											co.task = new WaitConfirm(p, confirmDelete).runTaskLaterAsynchronously(Main.main, 300); // 15*20 = 300 ticks
										} else {
											int time = (int) (15 - Math.floor((System.currentTimeMillis() - co.when) / 1000f));
											p.sendMessage("§cTape la commande " + args[0] + " delete confirm dans les " + time + " secondes pour confirmer la suppression de l'île " + is.isid.str());
										}
									}
								} else p.sendMessage("§cTu n'es pas le propriétaire de cette île !");
							}
						} else p.sendMessage("§cTu n'es pas dans l'overworld des îles !");
						break;
					}


					case "help": {
						p.sendMessage("§6Liste des sous-commandes :");
						p.sendMessage("§bCommandes de bases :");
						p.sendMessage(new ChatComponent("§e- go/home [numero]").setTextHover("§6pour te téléporter à ton île").create());
						p.sendMessage(new ChatComponent("§e- chat").setTextHover("§6pour parler avec les membres de l'île").create());
						p.sendMessage(new ChatComponent("§e- list").setTextHover("§6voir tes îles, et choisir l'île par défaut").create());
						p.sendMessage(new ChatComponent("§e- sethome").setTextHover("§6pour redéfinir le spawn de ton île").create());
						p.sendMessage(new ChatComponent("§e- top").setTextHover("§6pour voir le top 10 des îles !").create());
						p.sendMessage(new ChatComponent("§e- help").setTextHover("§6pour voir cette liste. Très surprenant.").create());
						p.sendMessage("§bCommandes d'équipe :");
						p.sendMessage(new ChatComponent("§e- team").setTextHover("§6pour voir l'équipe de ton île").create());
						p.sendMessage(new ChatComponent("§e- invite").setTextHover("§6pour inviter un joueur sur l'île").create());
						p.sendMessage(new ChatComponent("§e- kick").setTextHover("§6pour exclure un membre de l'île").create());
						p.sendMessage(new ChatComponent("§e- promote").setTextHover("§6pour augmenter le grade d'un membre").create());
						p.sendMessage(new ChatComponent("§e- demote").setTextHover("§6pour diminuer le grade d'un membre").create());
						p.sendMessage(new ChatComponent("§e- ban").setTextHover("§6pour bannir quelqu'un de île").create());
						p.sendMessage(new ChatComponent("§e- unban").setTextHover("§6pour débannir quelqu'un de l'île").create());
						p.sendMessage("§bBanque d'île :");
						p.sendMessage(new ChatComponent("§e- money/bank").setTextHover("§6pour voir la valeur de la banque d'île").create());
						p.sendMessage(new ChatComponent("§e- deposit").setTextHover("§6pour poser de l'argent bien au chaud dans la banque d'île").create());
						p.sendMessage(new ChatComponent("§e- withdraw").setTextHover("§6pour récupérer de l'argent de la banque d'île").create());
						p.sendMessage("§cCommandes dangereuses :");
						p.sendMessage(new ChatComponent("§e- setowner").setTextHover("§6pour changer la propriété de l'île").create());
						p.sendMessage(new ChatComponent("§e- delete").setTextHover("§6pour supprimer l'île").create());
						break;
					}
					default: {
						p.sendMessage("§cL'argument §4" + args[0] + "§c n'existe pas ! Fait /is help pour voir la liste des commandes");
						break;
					}
				}
			}
		}
	return true;
	}
}
