package fr.entasia.skycore.objs.isutils;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

public enum Extensions {
	X100(11, "§7100x100 (par défaut)", Material.IRON_BLOCK, 0, 0, "§7La taille par défaut de ton île. C'est quand même assez grand pour commencer !"),
	X200(12, "§3200x200", Material.LAPIS_BLOCK, 100, 100, "§3Tu devrais pouvoir faire des grands champs et de bonnes récoltes avec ca !"),
	X300(14, "§6300x300", Material.GOLD_BLOCK, 250, 250, "§6Une bonne taille pour faire de belles villes flottantes !"),
	X400(15, "§b400x400", Material.DIAMOND_BLOCK, 500, 500, "§bJe vois pas à quoi ca pourrait te servir mais bon, tu pourra te la péter !");

	public int slot;
	public String name;
	public Material type;
	public int minlvl;
	public int price;
	public ArrayList<String> lore;

	Extensions(int slot, String name, Material type, int price, int minlvl, String... lore) {
		this.slot = slot;
		this.name = name;
		this.type = type;
		this.minlvl = minlvl;
		this.price = price;
		this.lore = new ArrayList<>(Arrays.asList(lore));
		this.lore.add("");
		this.lore.add("§ePrix : " + price + "$");
		this.lore.add("§eNiveau requis : " + minlvl);
	}
}
