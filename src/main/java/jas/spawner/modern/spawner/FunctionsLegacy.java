package jas.spawner.modern.spawner;

import jas.spawner.modern.spawner.tags.Context;
import jas.spawner.modern.spawner.tags.TagsLegacy;
import net.minecraft.world.World;

/**
 * Tags styled in the old manner. Simple functions that could be easily done in MVEL but are provided for familiarity
 */
public class FunctionsLegacy implements TagsLegacy {
	private World world;
	public Context parent;

	public FunctionsLegacy(World world, Context parent) {
		this.world = world;
		this.parent = parent;
	}

	public boolean height(int minHeight, int maxHeight) {
		return parent.util().inRange(parent.posY(), minHeight, maxHeight);
	}

	public boolean light(int minLight, int maxLight) {
		return parent.util().inRange(parent.obj().light(), minLight, maxLight);
	}

	public boolean torchlight(int minLight, int maxLight) {
		return parent.util().inRange(parent.obj().torchlight(), minLight, maxLight);
	}

	public boolean origin(int minDistance, int maxDistance) {
		return parent.util().inRange(parent.obj().origin(), minDistance, maxDistance);
	}

	public boolean top() {
		return parent.wrld().biomeTop(parent.posX(), parent.posZ()) == parent.wrld().blockAt(parent.posX(),
				parent.posY() - 1, parent.posZ());
	}

	public boolean filler() {
		return parent.wrld().biomeFiller(parent.posX(), parent.posZ()) == parent.wrld().blockAt(parent.posX(),
				parent.posY() - 1, parent.posZ());
	}

	public boolean dimension(int dimension) {
		return parent.wrld().dimension() == dimension;
	}

	public boolean location(int[] offset, int[] range) {
		return parent.util().inRange(parent.posX(), offset[0] - range[0], offset[0] + range[0])
				&& parent.util().inRange(parent.posY(), offset[1] - range[1], offset[1] + range[1])
				&& parent.util().inRange(parent.posZ(), offset[2] - range[2], offset[2] + range[2]);
	}

	public boolean players(int[] searchRange, int[] minMaxBounds) {
		return parent.util().inRange(parent.obj().playersInRange(searchRange[0], searchRange[1]), minMaxBounds[0],
				minMaxBounds[1]);
	}

	public boolean entities(String[] searchNames, int[] searchRange, int[] minMaxBounds) {
		return parent.util().inRange(parent.obj().countEntitiesInRange(searchNames, searchRange[0], searchRange[1]),
				minMaxBounds[0], minMaxBounds[1]);
	}

	public boolean difficulty(int desiredDifficulty) {
		return desiredDifficulty == parent.obj().difficulty();
	}
	
	/* True if [0, range - 1] + offset <= maxValue */
	public boolean random(int range, int offset, int maxValue) {
		return parent.util().rand(range) + offset <= maxValue;
	}
}
