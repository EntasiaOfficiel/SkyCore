package fr.entasia.skycore.others.tasks;

import fr.entasia.skycore.Utils;
import fr.entasia.skycore.apis.BaseIsland;
import org.bukkit.scheduler.BukkitRunnable;

public class RankTask extends BukkitRunnable {

	public static BaseIsland[] list;

	@Override
	public void run() {
		BaseIsland[] list = new BaseIsland[10];
		for(int i=0;i<list.length;i++) {
			list[i] = new BaseIsland(); // fake
		}

		for(int i=0;i<list.length;i++) {
			check:
			for (BaseIsland is : Utils.islandCache) {
				if (list[i].getLevel() < is.getLevel()) {
					for(BaseIsland is2 : list){
						if(is2==is)continue check;
					}
					list[i] = is;
				}
			}
		}
		RankTask.list = list;
	}
}
