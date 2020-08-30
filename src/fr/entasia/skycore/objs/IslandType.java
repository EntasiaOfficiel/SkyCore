package fr.entasia.skycore.objs;

import org.bukkit.Material;
import org.bukkit.block.Biome;

public enum IslandType {
	DEFAULT(0, Biome.PLAINS, Material.WOOD_DOOR),
	ICE(1, Biome.ICE_FLATS, Material.SPRUCE_DOOR_ITEM),
	DESERT(2, Biome.DESERT, Material.BIRCH_DOOR_ITEM),
	JUNGLE(3, Biome.JUNGLE, Material.JUNGLE_DOOR_ITEM),
	MESA(4, Biome.MESA, Material.ACACIA_DOOR_ITEM),
	SWAMP(5, Biome.SWAMPLAND, Material.DARK_OAK_DOOR_ITEM);

	public int id;
	public IslandShematics schems;
	public Biome biome;
	public Material door;

	IslandType(int id, Biome biome, Material door){
		this.id = id;
		this.biome = biome;
		this.door = door;
	}

	public static IslandType getType(int i){
		return IslandType.values()[i];
	}

}
