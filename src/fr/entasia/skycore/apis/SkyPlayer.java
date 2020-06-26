package fr.entasia.skycore.apis;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.others.enums.Dimensions;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class SkyPlayer {

	public final UUID uuid;
	public final String name;
	public Player p;

	protected ArrayList<ISPLink> islands = new ArrayList<>();
	protected ArrayList<BaseIsland> invites = new ArrayList<>();
	protected ISPLink defaultis;
	protected long money;

	// online stuff
	public boolean islandChat = false;
	public boolean generating = false;


	// CONSTRUCTEURS


	public SkyPlayer(UUID uuid, String name){
		this.uuid = uuid;
		this.name = name;
	}

	public SkyPlayer(Player p){
		this.uuid = p.getUniqueId();
		this.name = p.getDisplayName();
	}


	// FONCTIONS UTILES

	@Nullable
	public ISPLink referentIsland(boolean checkLoc){
		if (islands.size() == 1) return islands.get(0);

		if(checkLoc){
			if(Dimensions.isIslandWorld(p.getWorld())){
				ISID isid = CooManager.getIslandID(p.getLocation());
				ISPLink link = getIsland(isid);
				if (link != null) return link;
			}
		}

		if (defaultis != null) return defaultis;
		return null;
	}

	public boolean isOnline(){
		return p!=null&&p.isOnline();
	}

	// FONCTIONS A AVOIR


	public boolean equals(SkyPlayer sp){
		return sp.uuid.equals(uuid);
	}

	public int hashCode(){
		return uuid.hashCode();
	}

	@Override
	public String toString() {
		return "SkyPlayer["+name+"]";
	}


	// FONCTIONS RANDOM

	public ArrayList<ISPLink> getIslands() {
		return new ArrayList<>(islands);
	}

	public ISPLink getIsland(ISID isid) {
		for(ISPLink link : islands){
			if(link.is.isid.equals(isid))return link;
		}
		return null;
	}

	public ISPLink getDefaultIS(){
		return defaultis;
	}

	public boolean setDefaultIS(ISID isid){
		ISPLink link = getIsland(isid);
		if(link==null)return false;
		else{
			defaultis = link;
			if(InternalAPI.SQLEnabled()){
				Main.sql.fastUpdate("update sky_pis set def = IF(x=? and z=?, 1, 0) where uuid=?", link.is.isid.x, link.is.isid.z, uuid);
			}
			return true;
		}
	}


	public long getMoney(){
		return money;
	}

	public void setMoney(long m){
		money=m;
		internalA();
	}

	public void addMoney(long m){
		money+=m;
		internalA();
	}


	public void withdrawMoney(long m){
		money-=m;
		internalA();
	}

	private void internalA(){
		if(InternalAPI.SQLEnabled())Main.sql.fastUpdate("UPDATE sky_players SET money=? WHERE uuid=?", money, uuid);
	}

	public ArrayList<BaseIsland> getInvites(){
		return new ArrayList<>(invites);
	}

	public BaseIsland getInvite(ISID isid){
		for(BaseIsland is : invites){
			if(is.equals(isid))return is;
		}
		return null;
	}
}
