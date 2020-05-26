package fr.entasia.skycore.otherobjs;

import com.boydti.fawe.object.schematic.Schematic;

public class IslandShematics {

	public String name;
	public Schematic island;
	public String[] plans;
	public Schematic[] structures;
	public Schematic[] miniIslands;


	public int indexOf(String a){
		for(int i=0;i<plans.length;i++){
			if(plans[i].equals(a))return i;
		}
		return -1;
	}

}
