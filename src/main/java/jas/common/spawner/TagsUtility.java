package jas.common.spawner;

import net.minecraft.world.World;

/**
 * Tags that serve no purpose but as intermediate methods for calculations
 */
public class TagsUtility {
	private World world;
	private Tags parent;

	public static abstract class Conditional {
		public abstract boolean isMatch(World world, int xCoord, int yCoord, int zCoord);
	}

	public TagsUtility(World world, Tags parent) {
		this.world = world;
		this.parent = parent;
	}

	public boolean inRange(int current, int minRange, int maxRange) {
		boolean isValid = !(current <= maxRange && current >= minRange);
		if (minRange <= maxRange) {
			isValid = (current <= maxRange && current >= minRange);
		} else {
			isValid = !(current < minRange && current > maxRange);
		}
		return isValid;
	}

	public boolean searchAndEvaluateBlock(Conditional condition, Integer[] searchRange, Integer[] searchOffsets) {
		Integer xRange = searchOffsets.length == 3 ? searchOffsets[0] : searchOffsets[0];
		Integer yRange = searchOffsets.length == 3 ? searchOffsets[1] : searchOffsets[0];
		Integer zRange = searchOffsets.length == 3 ? searchOffsets[2] : searchOffsets[0];

		Integer xOffset = searchOffsets.length == 3 ? searchOffsets[0] : searchOffsets[0];
		Integer yOffset = searchOffsets.length == 3 ? searchOffsets[1] : searchOffsets[0];
		Integer zOffset = searchOffsets.length == 3 ? searchOffsets[2] : searchOffsets[0];

		for (int i = -xRange; i <= xRange; i++) {
			for (int k = -zRange; k <= zRange; k++) {
				for (int j = -yRange; j <= yRange; j++) {
					if (condition.isMatch(world, i, j, k)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int rand(int value) {
		return world.rand.nextInt(value);
	}
}
