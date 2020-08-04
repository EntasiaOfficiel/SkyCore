package fr.entasia.skycore.invs;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.apis.other.ItemBuilder;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.others.tasks.RankTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OtherMenus {

	public static MenuCreator topRankMenu = new MenuCreator() {
		@Override
		public void onMenuClick(MenuClickEvent e) {

		}
	};
	public static void topRankOpen(Player p){

		Inventory inv = topRankMenu.createInv(6, "§6Top 10 des niveau d'îles :");


		ItemStack item;
		BaseIsland is;
		Entry e;
		for(int i=0;i<entries.length;i++){
			is = RankTask.list[i];
			if(is.getLevel()==0)break;
			e = entries[i];
			item = new ItemBuilder(Material.SKULL_ITEM).damage(3).name(e.color+"Top "+(i+1)).lore(is.getName(), "§aNiveau : "+
					is.getLevel(), "§cChef §6: "+is.getOwner().sp.name).build();
			inv.setItem(e.slot, item);
		}

		p.openInventory(inv);
	}

	public static Entry[] entries = new Entry[RankTask.list.length];

	public static class Entry{
		public int slot;
		public ChatColor color;

		public Entry(int slot, ChatColor color){
			this.slot = slot;
			this.color = color;
		}
	}

	static{
		entries[0] = new Entry(4 , ChatColor.GOLD); // 1
		entries[1] = new Entry(11, ChatColor.YELLOW);
		entries[2] = new Entry(15, ChatColor.YELLOW); // 3
		entries[3] = new Entry(22, ChatColor.AQUA);
		entries[4] = new Entry(28, ChatColor.AQUA); // 5
		entries[5] = new Entry(34, ChatColor.BLUE);
		entries[6] = new Entry(39, ChatColor.BLUE); // 7
		entries[7] = new Entry(41, ChatColor.BLUE);
		entries[8] = new Entry(45, ChatColor.BLUE); // 9
		entries[9] = new Entry(53, ChatColor.BLUE);
	}


}
