package fr.entasia.skycore.invs;

import fr.entasia.apis.ItemUtils;
import fr.entasia.apis.TextUtils;
import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.skycore.Main;
import fr.entasia.skycore.apis.ISPLink;
import fr.entasia.skycore.apis.SkyPlayer;
import fr.entasia.skycore.apis.TerrainManager;
import fr.entasia.skycore.others.enums.IslandType;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class IsMenus {

	// MENU CHOISIR UNE ILE DE DEPART

	private static final MenuCreator startIslandChooseMenu = new MenuCreator(null, null) {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			SkyPlayer sp = (SkyPlayer)e.data;
			e.player.closeInventory();
			switch(e.item.getType()){
				case GRASS:
					TerrainManager.generateIsland(sp, IslandType.DEFAULT);
					break;
				case ICE:
					TerrainManager.generateIsland(sp, IslandType.ICE);
					break;
				case SAND:
					TerrainManager.generateIsland(sp, IslandType.DESERT);
					break;
				case VINE:
					TerrainManager.generateIsland(sp, IslandType.JUNGLE);
					break;
				case HARD_CLAY:
					TerrainManager.generateIsland(sp, IslandType.MESA);
					break;
				case WATER_LILY:
					TerrainManager.generateIsland(sp, IslandType.SWAMP);
					break;
				default:
					e.player.sendMessage("§cErreur ! Cette ile n'a pas été correctement configurée ! Préviens un membre du Staff");
			}
			e.player.closeInventory();
		}
	};


	public static void startIslandChooseOpen(SkyPlayer sp){
		Inventory inv = startIslandChooseMenu.createInv(3, "§6Quelle île veut-tu ?", sp);

		ItemStack item = new ItemStack(Material.GRASS);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§aIle Basique");
		item.setItemMeta(meta);
		inv.setItem(10, item);

		item = new ItemStack(Material.ICE);
		meta = item.getItemMeta();
		meta.setDisplayName("§bIle des Neiges");
		item.setItemMeta(meta);
		inv.setItem(20, item);

		item = new ItemStack(Material.SAND);
		meta = item.getItemMeta();
		meta.setDisplayName("§aIle de Sable");
		item.setItemMeta(meta);
		inv.setItem(12, item);

		item = new ItemStack(Material.VINE);
		meta = item.getItemMeta();
		meta.setDisplayName("§2Ile Jungle");
		item.setItemMeta(meta);
		inv.setItem(14, item);

		item = new ItemStack(Material.HARD_CLAY);
		meta = item.getItemMeta();
		meta.setDisplayName("§6Ile Mesa");
		item.setItemMeta(meta);
		inv.setItem(24, item);

		item = new ItemStack(Material.WATER_LILY);
		meta = item.getItemMeta();
		meta.setDisplayName("§2Ile des Marais");
		item.setItemMeta(meta);
		inv.setItem(16, item);

		sp.p.openInventory(inv);
	}

	// MENU CHOISIR IS PAR DEFAUT


	private static final MenuCreator chooseDefaultIsland = new MenuCreator(null, null) {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			ISPLink chosen = (ISPLink) ((HashMap)e.data).get(e.slot);
			if(chosen==null)e.player.sendMessage("§cUne erreur est survenue lors du choix de l'île par défaut !");
			else{
				chosen.sp.setDefaultIS(chosen.is.isid);
				e.player.closeInventory();
				e.player.sendMessage("§aIle choisie avec succès !");
			}
		}
	};

	public static void chooseDefaultIslandOpen(SkyPlayer sp){
		HashMap<Integer, ISPLink> tracker = new HashMap<>();
		Inventory inv = chooseDefaultIsland.createInv(3, "§6Quelle île ?", tracker);

		int j = 10;
		ArrayList<String> list;
		for(ISPLink link : sp.getIslands()){
			ItemStack item = new ItemStack(Material.SAPLING);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("§aIle ");
			list = new ArrayList<>();
			list.add("§eID: "+link.is.isid.str());
			if(link.is.getName()!=null)list.add("§eNom : "+link.is.getName());
			list.add("§eRang: "+link.getName());
			meta.setLore(list);
			item.setItemMeta(meta);
			inv.setItem(j, item);
			tracker.put(j, link);
			j+=2;
		}

		sp.p.openInventory(inv);
	}

	// MENU IS DE BASE DE L'ILE

	private static final MenuCreator baseIslandMenu = new MenuCreator(null, null) {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			ISPLink link  = (ISPLink)e.data;
			e.player.closeInventory();
			switch(e.item.getType()){
				case SKULL_ITEM:{
					manageTeamOpen(link);
					break;
				}
				case BOOK:{
					e.player.sendMessage("§cCette option n'est pas encore prête !");
					// A FAIRE
					break;
				}
				default:{
					if(e.item.getType()==link.is.type.door){
						e.player.teleport(link.is.getHome());
						e.player.sendMessage("§eTu as été téléporté à ton île !");
					}
					break;
				}
			}
		}
	};


	public static void baseIslandOpen(ISPLink link) {

		Inventory inv = baseIslandMenu.createInv(5, "§6Menu principal de l'île :", link);

		ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 11);
		for (int i = 0; i < 45; i += 9) {
			inv.setItem(i, item);
			inv.setItem(i + 8, item);
		}
		for (int i = 36; i < 45; i++) inv.setItem(i, item);

		item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
		for (int i = 1; i < 8; i++) inv.setItem(i, item);


		item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Informations");
		ArrayList<String> a = new ArrayList<>();
		a.add("§eID : " + link.is.isid.x + ";" + link.is.isid.z);
		a.add("§eNuméro : " + link.sp.getIslands().indexOf(link));
		ArrayList<ISPLink> members = link.is.getMembers();
		if(members.size()==1)a.add("§eAucune équipe !");
		else a.add("§eÉquipe : "+members.size()+" membres");
		a.add("§eRôle : "+TextUtils.firstLetterUpper(link.getRank().name));
		meta.setLore(a);
		item.setItemMeta(meta);
		inv.setItem(4, item);

		item = new ItemStack(link.is.type.door);
		meta = item.getItemMeta();
		meta.setDisplayName("§aSe téléporter à l'île");
		item.setItemMeta(meta);
		inv.setItem(19, item);

		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta smeta = (SkullMeta) skull.getItemMeta();
		smeta.setDisplayName("§eVoir l'équipe de ton île");
		skull.setItemMeta(smeta);
		ItemUtils.placeSkullAsync(inv, 20, skull, link.sp.p, Main.main);

		item = new ItemStack(Material.BOOK);
		meta = item.getItemMeta();
		meta.setDisplayName("§6Challenges");
		item.setItemMeta(meta);
		inv.setItem(33, item);

		link.sp.p.openInventory(inv);
	}


	// MENU DE TEAM


	private static final MenuCreator manageTeamMenu = new MenuCreator(null, null) {

		@Override
		public void onMenuClick(MenuClickEvent e) {
			ISPLink link = (ISPLink)e.data;
			if(e.item.getType()==Material.BOOK_AND_QUILL) baseIslandOpen(link);
		}
	};

	public static void manageTeamOpen(ISPLink link){
		Inventory inv = manageTeamMenu.createInv(3, "§6Ton équipe :", link);

		int i = 0;
		for(ISPLink ll : link.is.getSortedMembers()){

			ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
			SkullMeta smeta = (SkullMeta) item.getItemMeta();

			smeta.setDisplayName(ll.getName());
			if(ll.equals(link))smeta.setLore(Collections.singletonList("§aC'est toi !"));
			item.setItemMeta(smeta);
			ItemUtils.placeSkullAsync(inv, i, item, ll.sp.name, Main.main);
			i++;
		}

		ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§cRetour au menu précédent");
		item.setItemMeta(meta);
		inv.setItem(26, item);

		link.sp.p.openInventory(inv);
	}
}
