package fr.entasia.skycore.commands;

import fr.entasia.apis.other.ChatComponent;
import fr.entasia.apis.utils.TextUtils;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.*;
import fr.entasia.skycore.invs.IsMenus;
import fr.entasia.skycore.objs.CodePasser;
import fr.entasia.skycore.others.enums.Dimensions;
import fr.entasia.skycore.others.enums.MemberRank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

import static fr.entasia.skycore.commands.IsCmdUtils.*;
//import static fr.entasia.skycore.commands.IsCmdUtils.*;

public class IsCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if(!(sender instanceof Player))return true;
		Player p = ((Player)sender);
		SkyPlayer sp = BaseAPI.getOnlineSP(p.getUniqueId());
		if(sp==null){
			p.sendMessage("§cTon profil est mal chargé ! Contacte un Membre du Staff");
			return true;
		}
		final ISPLink link = sp.referentIsland(true);
		if (args.length == 0) {
			if (sp.getIslands().size()==0) IsMenus.startIslandChooseOpen(sp);
			else if(link==null) {
				p.sendMessage("§cTu dois d'abord choisir une île préférée pouvoir choisir ces options ! (On ne sait pas de laquelle tu parles !)");
				IsMenus.chooseDefaultIslandOpen(sp);
			}else IsMenus.baseIslandOpen(link);
			return true;
		}
		args[0] = args[0].toLowerCase();
		switch (args[0]) {
			case "dis":
			case "defaultis":{
				IsMenus.chooseDefaultIslandOpen(sp);
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
						if(args.length>1){
							try{
								int index = Integer.parseInt(args[1]);
								if(index<=0)p.sendMessage("§cCe numéro d'île est invalide ! (0/Négatif)");
								else{
									ArrayList<ISPLink> list = link.sp.getIslands();
									if(list.size()<index)p.sendMessage("§cAucune île ne correspond à ce numéro d'île !");
									else{
										ISPLink newLink = list.get(index-1);
										p.setFallDistance(0);
										p.teleport(newLink.is.getHome());
										p.sendMessage("§6Tu as été téléporté à ton île n° "+index+" !");
									}
								}
							}catch(NumberFormatException e){
								p.sendMessage("§cCe numéro d'île est invalide !");
							}
						}else{
							p.teleport(link.is.getHome());
							p.sendMessage("§6Tu as été téléporté à ton île !");
						}
						break;
					}
					case "team": {
						IsMenus.manageTeamOpen(link);
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
					case "lvl":{

						int a = link.is.updateLvl( new CodePasser.Void() {
							@Override
							public void run() {
								p.sendMessage("§aNiveau de l'île : "+link.is.getLevel());
								p.sendMessage("§aPoints demandés pour le niveau suivant : "+link.is.getRemPoints());
							}
						});
						if(a==0) p.sendMessage("§aCalcul du niveau de l'île en cours...");
						else p.sendMessage("§cTu dois encore attendre "+TextUtils.secondsToTime(a)+" avant de recalculer le niveau de l'île !");

						break;
					}

					case "leave": case "quit":{
						if(link.is.removeMember(link)){
							link.is.sendTeamMsg(link.getName()+"§e à quitté l'île !");
							p.sendMessage("§cTu as quitté l'île !");
						}else p.sendMessage("§cUne erreur s'est produite !");
						break;
					}

					case "invite":
					case "uninvite":
					case "kick":
					case "demote":
					case "promote": {
						if (link.getRank().id < MemberRank.ADJOINT.id)
							p.sendMessage("§cTu dois être au minimum adjoint pour gérer l'équipe de cette île !");
						else {
							if (args.length < 2) p.sendMessage("§cMet un joueur en argument !");
							else {
								SkyPlayer target = BaseAPI.getArgSP(sender, args[1], false);
								if (target != null) {
									if (target.equals(link.sp)) p.sendMessage("§cCe joueur est.. toi même ?");
									else {
										ISPLink targetLink = link.is.getMember(target.uuid);
										if (args[0].equals("invite")) {
											if (targetLink == null) {
												if (link.is.invitePlayer(target)) {
													link.is.sendTeamMsg("§3" + target.name + "§e à été invité sur l'île par " + link.getName() + "§e !");
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
													if (target.p != null) target.p.sendMessage("§cL'invitation de l'île §4" + link.is.getNameOrID() + "§c à été annulée !");
												} else p.sendMessage("§cCe joueur n'est pas invité !");
											} else
												p.sendMessage("§cCe joueur est un membre de l'île ! Utilise §4/is kick");
										} else if (targetLink == null)
											p.sendMessage("§cCe joueur n'est pas membre sur cette île !");
										else {
											switch (args[0]) {
												case "kick": {
													if (targetLink.getRank().id < link.getRank().id) {
														if (link.is.removeMember(targetLink))
															link.is.sendTeamMsg("§7" + targetLink.getName() + "§e à été expulsé de l'île par " + link.getName() + "§e !");
															if(target.p!=null)target.p.sendMessage("§cTu as été exclu de l'île par §3"+link.sp.name+"§c !");
														if(targetLink.sp.p!=null) targetLink.sp.p.teleport(Utils.spawn);
														else p.sendMessage("§cUne erreur s'est produite !");
													} else p.sendMessage("§cCette personne est trop haut gradée !");
													break;
												}

												case "promote": {
													if (targetLink.getRank().id + 1 < link.getRank().id) {
														MemberRank nrank = MemberRank.getType(targetLink.getRank().id + 1);
														if (link.is.reRankMember(targetLink, nrank))
															link.is.sendTeamMsg(targetLink.getName() + "§e à été promote par " + link.getName() + "§e !");
														else p.sendMessage("§cUne erreur s'est produite !");
													} else {
														if (link.getRank() == MemberRank.CHEF)
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
															if (link.is.reRankMember(targetLink, nrank))
																link.is.sendTeamMsg(targetLink.getName() + "§e à été demote par " + link.getName() + "§e !");
															else p.sendMessage("§cUne erreur s'est produite !");
														}
													} else p.sendMessage("§cCette personne est trop haut gradée !");
												}
												break;
											}
										}
									}
								}
							}
						}
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
											if (co == null || (System.currentTimeMillis() - co.when > 10000))
												p.sendMessage("§cLe temps de confirmation est écoulé !");
											else {
												co.task.cancel();
												confirmPassOwner.remove(p);
												if (co.is.isid.equals(is.isid)) {
													SkyPlayer target = BaseAPI.getArgSP(link.sp.p, args[1], true);
													if (target != null) {
														if (target.equals(link.sp))
															p.sendMessage("§cCe joueur est.. toi-même ?");
														else {
															ISPLink newLink = link.sp.getIsland(is.isid);
															if (newLink == null)
																p.sendMessage("§cCe joueur n'est plus membre sur cette île !");
															else {
																is.sendTeamMsg("§3Passage du chef sur cette île à §c" + link.sp.p + " §3!");
																is.reRankMember(newLink, MemberRank.CHEF);
															}
														}
													}
												} else
													p.sendMessage("§cL'île sur laquelle tu es n'est pas la même que celle ou tu as fait la première commande ! Annulation");
											}
										} else {
											if (co == null) {
												SkyPlayer target = BaseAPI.getArgSP(p, args[1], true);
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
												int time = (int) Math.ceil((15 - System.currentTimeMillis() - co.when) / 1000f);
												p.sendMessage("§cTape la commande §4" + command.getName() + " setowner " + args[1] + " confirm§c dans les " + time +
														" secondes pour confirmer lae changement de propriétaire. " + is.isid.str());
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
												BaseAPI.deleteIsland(is, new CodePasser.Bool() {
													@Override
													public void run(boolean err) {
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
											int time = (int) Math.ceil((15 - System.currentTimeMillis() - co.when) / 1000f);
											p.sendMessage("§cTape la commande " + args[0] + " delete confirm dans les " + time + " secondes pour confirmer la suppression de l'île. " + is.isid.str());
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
						p.sendMessage("§e- go/home [numero] §6pour te téléporter à ton île");
						p.sendMessage("§e- chat §6pour parler avec les membres de l'île");
						p.sendMessage("§e- defaultis §6choisit l'île par défaut pour tes commandes");
						p.sendMessage("§e- help §6Pour voir cette liste. Très surprenant.");
						p.sendMessage("§bCommandes d'équipe :");
						p.sendMessage("§e- team §6pour voir l'équipe de ton île");
						p.sendMessage("  §e- invite §6pour inviter un joueur sur l'île");
						p.sendMessage("  §e- kick §6pour exclure un membre de l'île");
						p.sendMessage("  §e- promote §6pour augmenter le grade d'un membre");
						p.sendMessage("  §e- demote §6pour diminuer le grade d'un membre");
						p.sendMessage("§cCommandes dangereuses :");
						p.sendMessage("§e- setowner §6pour changer la propriété de l'île");
						p.sendMessage("§e- delete §6pour supprimer l'île");
						break;
					}
					default: {
						p.sendMessage("§cL'argument " + args[0] + " n'existe pas !");
						break;
					}
				}
			}
		}
	return true;
	}
}
