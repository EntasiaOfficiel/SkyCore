package fr.entasia.skycore.apis;

import fr.entasia.skycore.Main;
import fr.entasia.skycore.others.enums.MemberRank;

public class ISPLink {

	public final BaseIsland is;
	public final SkyPlayer sp;
	protected MemberRank rank;

	public ISPLink(BaseIsland is, SkyPlayer sp, MemberRank rank){
		this.is = is;
		this.sp = sp;
		this.rank = rank;
	}

	public boolean setRank(MemberRank rank){
		if(rank==MemberRank.DEFAULT){
			InternalAPI.warn("Utilise removeMember() pour supprimer un joueur de l'île !", true);
			return false;
		}
		this.rank = rank;
		if(rank==MemberRank.CHEF) {
			is.owner.setRank(MemberRank.ADJOINT);
			is.owner = this;
		}
		if(InternalAPI.SQLEnabled()) Main.sql.fastUpdate("UPDATE sky_pis SET rank = ? WHERE uuid=? and x=? and z=?", rank.id, sp.uuid, is.isid.x, is.isid.z);
		return true;
	}

	public MemberRank getRank(){
		return rank;
	}

	public String getName(){
		return rank.getName()+"§7 "+sp.name;
	}

	public boolean equals(ISPLink l){
		return l.is.equals(is)&&l.sp.equals(sp);
	}
}
