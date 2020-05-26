package fr.entasia.skycore.apis;

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

	public void setRank(MemberRank rank){
		this.rank = rank;
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
