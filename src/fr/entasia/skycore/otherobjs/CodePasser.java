package fr.entasia.skycore.otherobjs;

import fr.entasia.skycore.apis.BaseIsland;
import org.bukkit.entity.Player;

public class CodePasser {

	public abstract static class Inv {
		abstract public void run(Player p, BaseIsland is);
	}

	public static abstract class Void {
		abstract public void run();
	}

	public static abstract class Bool {
		abstract public void run(boolean b);
	}

}
