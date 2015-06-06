package jas.spawner.modern.eventspawn.context;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.IGrowable;
import net.minecraft.world.World;

/**
 * Encapsulate Minecraft Checks
 */
public class ContextHelper {
	public static boolean isBlockCrop(Block block, int blockMetadata) {
		return block instanceof IGrowable;
	}

	public static boolean isBlockTree(World world, int posX, int posY, int posZ, Block block, int blockMetadata) {
		return block instanceof BlockLog || block.isWood(world, posX, posY, posZ);
	}
}
