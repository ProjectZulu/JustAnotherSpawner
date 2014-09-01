package jas.common.spawner;

import net.minecraft.block.Block;
import net.minecraft.world.World;

/**
 * Tags styled in the old manner. Simple functions that could be easily done in MVEL but are provided for familiarity
 */
public class LegacyTags {
	private World world;
	public Tags parent;

	public LegacyTags(World world, Tags parent) {
		this.world = world;
		this.parent = parent;
	}

	public boolean height(int minHeight, int maxHeight) {
		return parent.util.inRange(parent.posY, minHeight, maxHeight);
	}

	public boolean light(int minLight, int maxLight) {
		return parent.util.inRange(parent.obj.light(), minLight, maxLight);
	}

	public boolean torchlight(int minLight, int maxLight) {
		return parent.util.inRange(parent.obj.torchlight(), minLight, maxLight);
	}

	public boolean origin(int minDistance, int maxDistance) {
		return parent.util.inRange(parent.obj.origin(), minDistance, maxDistance);
	}

	public boolean top() {
		return parent.wrld.biomeTop(parent.posX, parent.posZ) == parent.wrld.blockAt(parent.posX, parent.posY - 1,
				parent.posZ);
	}

	public boolean filler() {
		return parent.wrld.biomeFiller(parent.posX, parent.posZ) == parent.wrld.blockAt(parent.posX, parent.posY - 1,
				parent.posZ);
	}

	public boolean dimension(int dimension) {
		return parent.wrld.dimension() == dimension;
	}

	public boolean location(int[] target, int[] variane) {
		return parent.util.inRange(parent.posX, target[0] - variane[0], target[0] + variane[0])
				&& parent.util.inRange(parent.posY, target[1] - variane[1], target[1] + variane[1])
				&& parent.util.inRange(parent.posZ, target[2] - variane[2], target[2] + variane[2]);
	}

	public boolean players(int[] searchRange, int[] minMaxBounds) {
		return parent.util.inRange(parent.obj.playersInRange(world, searchRange[0], searchRange[1]), minMaxBounds[0],
				minMaxBounds[1]);
	}

	public boolean entities(String[] searchNames, int[] searchRange, int[] minMaxBounds) {
		return parent.util.inRange(parent.obj.countEntitiesInRange(searchNames, searchRange[0], searchRange[1]),
				minMaxBounds[0], minMaxBounds[1]);
	}

	public boolean difficulty(int desiredDifficulty) {
		return desiredDifficulty == parent.obj.difficulty();
	}
}
