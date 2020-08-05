package fr.entasia.skycore;

import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.SkyPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Utils {
	public static final int ISSIZE = 400;
	public static String guessWord = null;

	public static World spawnWorld;
	public static Location spawn;

	public static ArrayList<Player> masterEditors = new ArrayList<>();

	public static ArrayList<BaseIsland> islandCache = new ArrayList<>();
	public static ArrayList<SkyPlayer> playerCache = new ArrayList<>();
	public static ArrayList<SkyPlayer> onlineSPCache = new ArrayList<>();


	public static String formatMoney(long money){
		String a = Long.toString(money);
		StringBuilder b = new StringBuilder();
		char[] chars = a.toCharArray();
		for(int i=0;i<chars.length;i++){
			if(i%3==0)b.append(" ");
			b.append(chars[i]);
		}
		return b.substring(1)+"$";
	}


}
