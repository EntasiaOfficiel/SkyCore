package fr.entasia.skycore.objs.enums;

import fr.entasia.skycore.objs.IslandShematics;
import org.bukkit.World;

import javax.annotation.Nonnull;

public enum Dimensions {
	OVERWORLD(0),
	NETHER(1),
	END(2);
//	CLOUDS(3);

	public int id;
	public World world;
	public IslandShematics schems;

	Dimensions(int id){
		this.id = id;
	}

	@Nonnull
	public static Dimensions getDimension(World w){
		for(Dimensions d : Dimensions.values()){
			if(d.world==w)return d;
		}
		return null;
	}

	public static boolean isIslandWorld(World w){
		for(Dimensions d : Dimensions.values()){
			if(d.world!=null&&d.world==w)return true;
		}
		return false;
	}

}
