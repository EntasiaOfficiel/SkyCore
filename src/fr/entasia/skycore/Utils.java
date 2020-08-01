package fr.entasia.skycore;

import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.SkyPlayer;
import org.bukkit.GameMode;
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


	public static boolean isMasterEdit(Player p){
		return p.getGameMode()== GameMode.CREATIVE&&masterEditors.contains(p);
	}
}
