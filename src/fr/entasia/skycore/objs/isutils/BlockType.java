package fr.entasia.skycore.objs.isutils;

import org.bukkit.Material;

import java.util.HashMap;

public class BlockType {
	public HashMap<Integer, Integer> prices = new HashMap<>();
	public int others=0;

	public BlockType(int others){
		this.others = others;
	}

	public BlockType(){
	}

	public int getPrice(int meta){
		int a = prices.getOrDefault(meta, 0);
		if(a==0)return others;
		else return a;
	}



	@Override
	public String toString() {
		return "BlockType["+prices.size()+" prices]";
	}
}
