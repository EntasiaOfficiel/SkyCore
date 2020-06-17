package fr.entasia.skycore.commands;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class TestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!sender.hasPermission("*"))return true;
		Player p = (Player)sender;























//		int x = p.getLocation().getBlockX();
//		int y = p.getLocation().getBlockY();
//		int z = p.getLocation().getBlockZ();
//
//		Block b = p.getLocation().getBlock();
//		BlockState bs = b.getState();
//
//		net.minecraft.server.v1_12_R1.Chunk NMSChunk = ((CraftChunk)p.getLocation().getChunk()).getHandle();
//		World NMSWorld = NMSChunk.getWorld();
//
//		BlockPosition pos = new BlockPosition(x, y, z);
//		IBlockData data = Blocks.STONE.getBlockData();
//		int flag = 3;
//		BlockState state = NMSWorld.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getState();
//
////		NMSWorld.setTypeAndData(pos, data, flag);
//
//
////		NMSWorld.capturedBlockStates.add(state);
//		IBlockData data2 = NMSChunk.a(pos, data);
////		if(!(sender instanceof Player))return true;System.out.println("null="+(data2==null));
//
////		if (data.c() != data2.c() || data.d() != data2.d()) {
////			NMSWorld.methodProfiler.a("checkLight");
////			NMSChunk.runOrQueueLightUpdate(() -> {
////				NMSWorld.w(pos);
////			});
////			NMSWorld.methodProfiler.b();
////		}
//
//
////		PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(NMSChunk, 65535);
//		PacketPlayOutBlockChange a = new PacketPlayOutBlockChange(NMSWorld, pos);
//		((CraftPlayer) p).getHandle().playerConnection.sendPacket(a); // send the chunk
//
////		if (!NMSWorld.captureBlockStates) {
//
////		}
//		Bukkit.broadcastMessage("placed");
//

		return true;
	}
}