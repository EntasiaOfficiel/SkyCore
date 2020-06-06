package fr.entasia.skycore;



import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.IBlockData.*;

/*
   This class is intended to make it easy to rapidly place huge numbers of blocks at once (multiple whole-chunk-sized structures)
   It was created to help with generating unusually large trees and structures during initial chunk generation

   Create a new FastBlockChanger object somewhere in your plugin (you should only need one per server, normally).
   Use it to access the getChunk method to get a chunk updater, and setBlock to place blocks in that ChunkUpdater.

   After a few seconds without any block placement, the chunk will be relit where needed and refreshed for players.


   getChunk(net.minecraft.server.World w, int chunkX, int chunkZ, boolean updateIfUnchanged)
   getChunk(org.bukkit.World w,  int chunkX, int chunkZ, boolean updateIfUnchanged)
     Gets a chunk updater for the chunk at a location. update determines if the chunk should resend to players even if it is not modified.
     Use >>4 (right bitshift 4) to get a chunk coordinate from a block coordinate. example: getChunk(world, posX >> 4, posZ >> 4, false)
     Getting a chunk with this method also registers it with the updater.
     Calling getChunk multiple times on the same chunk does not duplicate it or create inefficiency, but it should only be done once due to lookup times, unless it is needed in multiple scopes.

     A ChunkUpdater object will trigger relighting of blocks and send them to players after three seconds without any changes.
     After relighting and resending, you can still use it, calling setBlock will reset the lighting bitfields and add it to the update list again.

   ChunkUpdater.setBlock(BlockPosition b, IBlockData i)
     Sets a block in the chunk, far faster than if it was set with bukkit methods or net.minecraft.server.World.setTypeAndData
     The block is invisible to players and does not recalculate light
*/


public class FastBlockChanger {

	public ArrayList<ChunkUpdater> chunks = new ArrayList<ChunkUpdater>(64);

	// these control how far to smooth lighting along edges (-2 = 2 blocks in each direction if needed)
	final byte sLow = -2;// negative value

	final byte sHigh = (byte) (Math.abs(sLow)+1); // abs(sLow)+1
	public boolean enabled = true;

	public boolean isChunkSendable(int x, int z)
	{
		boolean r = true;
		for(ChunkUpdater c : chunks)
			if(c.x == x && c.z == z && !c.isFBCPacket)
				r=false;
		return r;
	}

	// get a chunkupdater from a bukkit world object
	public ChunkUpdater getChunk(org.bukkit.World cw, int cx, int cz, boolean updateIfUnchanged)
	{
		return getChunk( ((org.bukkit.craftbukkit.v1_12_R1.CraftWorld)cw).getHandle(),cx,cz,updateIfUnchanged);
	}

	// get a chunk updater from an internal world object
	public ChunkUpdater getChunk(net.minecraft.server.v1_12_R1.World cw, int cx, int cz, boolean updateIfUnchanged)
	{
		this.enabled=true;// wake up the task if needed
		for(ChunkUpdater c : chunks)
			if(c.x==cx && c.z==cz && c.w==cw)
				return c;

		if(!cw.areChunksLoaded(new BlockPosition(cx<<4, 0, cz<<4),1)) // do this after check, if this CU exists, it must be safe.. right?
			return new ChunkUpdater(cw,cx,cz,null,updateIfUnchanged); // prevents horrible chunk-loading stack overflows

		Chunk gch = cw.getChunkIfLoaded(cx, cz);
		if(gch.isDone())
			return new ChunkUpdater(cw,cx,cz,gch,updateIfUnchanged);
		else
			return new ChunkUpdater(cw,cx,cz,null,updateIfUnchanged);
	}

	public void runTask()
	{
		int i=chunks.size();
		System.out.println(i);
		if(i==0)
		{
			this.enabled=false; // stop the task if nothing is left to do
			return;
		}

		long end = System.currentTimeMillis();

		long done = end-3000; // 3 seconds without any block changes to resend a chunk

		end+=10; // time limit for this loop

		// make a setting for this
		if(i>3)
			i=3; // do no more than 3 chunks per cycle to prevent stuff going potato clientside

		ChunkUpdater cu;
		//Chunk ch;
		while(System.currentTimeMillis() < end && --i>-1) // iterate through the list backwards to delete entries without issues
		{
			System.out.println("loop");
			cu=chunks.get(i);
			System.out.println("1");
			if( cu.nChanged>0 || cu.updateIfUnchanged )
			{
			System.out.println("2");
				if(cu.chnk != null)
				{

					cu.recalcLighting();// determine what blocks need relighting, recalculate it
					//ch = cu.ch;
					System.out.println("updating");
					sendToPlayers(cu);

					cu.isUpdated=true;
					chunks.remove(i);// remove the chunk updater. it is not destroyed and will be reset and added again if modified further (if a reference is held elsewhere)
				}
			}else if(!cu.updateIfUnchanged && cu.nChanged == 0)
				chunks.remove(i);
			}
	}

	private short sendToPlayers(ChunkUpdater cu)
	{
		if(cu.w.getWorld().unloadChunk(cu.x, cu.z, true, true)) // nobody there? dont bother, save memory.
			return 0;

		byte n = 0;
		cu.isFBCPacket=true;
		byte d = (byte) (cu.chnk.world.spigotConfig.viewDistance*1.75f);
		//RpgLogger.info("gti "+(x<<4)+","+(z<<4)+"  "+((x<<4) + 16)+","+((z<<4) + 16));
		Collection<TileEntity> lti = cu.chnk.tileEntities.values();
		d*=d;
		PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(cu.chnk, 65535);
		//PacketPlayOutUnloadChunk puc = new PacketPlayOutUnloadChunk(cu.x,cu.z); // may be needed in some situations or for some clients
		ArrayList<Packet<?>> te = new ArrayList<>(16);

		for(TileEntity ti:lti)
		{
			Packet<?> p = ti.getUpdatePacket();
			if (p != null)
				te.add(p);
		}

		// get the playerchunk to see what players have the chunk loaded, more effective that trying to guess with view distance?
		PlayerChunk pc = ((net.minecraft.server.v1_12_R1.WorldServer)(cu.w)).getPlayerChunkMap().getChunk(cu.x, cu.z);

		ArrayList<EntityPlayer> pl = new ArrayList<EntityPlayer>(pc.c.size());
		if(pc.c!=null)
			pl.addAll(pc.c);

		if(pc==null||pl.size()==0)
		{
			//cu.w.getWorld().unloadChunk(cu.x, cu.z, true, false);
			}
		else
			for(EntityPlayer p : pl)
			{
				p.playerConnection.sendPacket(pmc); // send the chunk

				for(Packet<?> packet : te)
					p.playerConnection.sendPacket(packet); // send the tile entities
			}

		cu.isFBCPacket=false;

		return n;
	}

	// Represents a chunk and blocks in need of relighting
	public class ChunkUpdater
	{
		private short sections=0;
		public final int x,z; // location
		public final World w; // world
		public Chunk chnk; // the chunk this will update

		public long last_modified; // chunk update timer
		private  boolean updateIfUnchanged; // if true, update the chunk even if nothing was changed. this is for new chunks being generated (not implemented yet)
		private int nChanged; // total changed blocks in this chunk
		private boolean isUpdated=false;
		public boolean isFBCPacket=false; // block the chunk load packet?
		// x  z  y
		private long[][][] bits = new long[16][16][4]; // an array of long bitfields, each representing 64 blocks along Y-axis
		// 16x16x(4x64) is 65536 blocks, or one chunk

		private ChunkUpdater(World w, int x, int z, Chunk ch, boolean update)
		{
			this.updateIfUnchanged=update;
			this.nChanged=0;
			this.chnk=ch;
			this.w=w;
			this.x=x;
			this.z=z;
			this.last_modified=System.currentTimeMillis()+30000; // let unmodified chunks idle for 30 seconds before either deleting updater or resending chunk.
			chunks.add(this);
		}

		public void setBlock(BlockPosition b, IBlockData i) // example:  setBlock( new BlockPosition(x,y,z), Blocks.AIR.getBlockData() )
		{
			if(this.chnk==null)
			{
				this.chnk=this.w.getChunkIfLoaded(x, z);
				if(this.chnk==null)
					return;
			}

			int x = (b.getX()%16 + 16)%16;
			int z = (b.getZ()%16 + 16)%16;

			if( x > 15 || x < 0 || z > 15 || z < 0 || b.getY() > 255 || b.getY() < 0 )
				return; // tried to set a block outside the chunk

			if(!chnk.isDone())
			{
			}
			if(!chnk.bukkitChunk.isLoaded())
			{
				boolean l = chnk.bukkitChunk.load(false);
			}


			if(isUpdated) // this updated was used already and needs to be reset
			{
				sections=0;
				enabled=true; // wake up the enclosing task if needed
				isUpdated=false;
				bits = new long[16][16][4];
				chunks.add(this); // re-add to the list if it was already updated and removed
				last_modified=System.currentTimeMillis()+30000;// 30 second idle
			}
			else
				last_modified=System.currentTimeMillis();

			// You have to use NMS position and block for this, craftbukkit just cant do it, and casting from org.bukkit.Material is complex and inefficient.

			chnk.a(b,i); // set the block silently with net.minecraft.server.Chunk.a(BlockPosition,IBlockData).
			//If this does not exist in the current version, look in Chunk.java and find the current name for the method  IBlockData Something(BlockPosition, IBlockData)

			++nChanged;

			sections|=1<<(b.getY()>>4); // y-axis chunk sections modified

			long nb = ( (long)1 << (b.getY()%64) );
			////(a % b + b) % b fixes issues with java modulus giving negative numbers for negative values of a
			bits[x][z][b.getY()/64] |= nb; // set the relighting bit for the block
			//RpgLogger.info("set bit "+Long.toBinaryString(cb)+" at "+((b.getX()%16 + 16)%16)+","+((b.getZ()%16+16)%16)+","+(b.getY()/64)+" to "+Long.toBinaryString(nb)+" ("+Long.toBinaryString(cb|nb)+")");
		}

		private void relight(BlockPosition bp)
		{
			w.c(EnumSkyBlock.SKY, bp);
			w.c(EnumSkyBlock.BLOCK, bp);
		}

		private void finalizeRelightBlocks()
		{
			long[][][] newbits = new long[16][16][4]; // cant add more blocks to bits directly without it adding more blocks around the blocks we just added, ad infinitum
			int bx,bz,by,by64,px=x<<4,pz=z<<4;
			long bb;
			byte l;
			boolean trnsp=false;// is block transparent to light?

			// this sets relight bits on every transparent block plus first solid block below a block being relit

			for(bx=0; bx<16; ++bx)
				for(bz=0; bz<16; ++bz)
					for(trnsp=false,by=3; by>-1; --by)
					{
						l=64;
						by64=by*64;
						bb=bits[bx][bz][by];
						while(--l>-1)
						{
							if(trnsp)// air below updated block
							{
								if(w.getType(new BlockPosition(px+bx,l+by64,pz+bz)).getMaterial().isSolid())
								{
									newbits[bx][bz][by] |= ( (long)1 << by64 ); // set the relighting bit for the block
									trnsp=false;
								}
							}
							if((bb & (1L<<l)) > 0)
							{
								trnsp=true;
								newbits[bx][bz][by] |= ( (long)1 << by64 ); // set the relighting bit for the block
							}
						}
					}

			// add newbits to bits
			for(bx=0; bx<16; ++bx)
				for(bz=0; bz<16; ++bz)
					for(trnsp=false,by=3; by>-1; --by)
						bb = bits[bx][bz][by] | newbits[bx][bz][by]; // OR all the things
		}

		private void recalcLighting()
		{
			// fix lighting for all the new blocks and transparent blocks around them
			finalizeRelightBlocks();

			int bx,bz,by,by64,px=x<<4,pz=z<<4;
			long bb;
			byte l;
			BlockPosition bp;
			BlockPosition bpt;
			boolean rlt; // do relight on block?
			int gl,gl1;

			for(bx=0; bx<16; ++bx)
				for(bz=0; bz<16; ++bz)
					for(by=3; by>-1; --by)
					{
						l=64;
						by64=by*64;
						bb=bits[bx][bz][by];
						while(--l>-1)
						{
							if((bb & (1L<<l)) > 0)
							{
								bp = new BlockPosition(px+bx,l+by64,pz+bz);
								relight(bp);
								gl=w.getLightLevel(bp);

								int tx,tz;
								for(tx=-sLow;tx<sHigh;++tx)
									for(tz=sLow;tz<sHigh;++tz)
									{
										rlt=false;
										if(tx!=0 && tz!=0)// not the block being calculated already
										{
											if( !( tx+bx<0 || tx+bx>15 || tz+bz<0 || tz+bz>15) ) // not outside the chunk
												if((bits[tx+bx][tz+bz][by]&(1L<<l)) == 0) // not being relit already
													rlt=true;
												else
													rlt=true;
										}
										if(rlt)//relight the block
										{
											bpt=bp.east(tx).north(tz);
											gl1=w.getLightLevel(bpt);

											if(Math.abs(gl-gl1) > 3) // relight if the light levels are quite different
											{
												relight(bpt);
											}
										}
									}
							}
						}
					}
			isUpdated=true;// removed from the list and inactive
		}
	}
}