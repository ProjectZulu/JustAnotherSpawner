package jas.spawner.modern.spawner;

import jas.common.JASLog;
import net.minecraft.block.material.Material;
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
		Integer xRange = searchRange.length == 3 ? searchRange[0] : searchRange[0];
		Integer yRange = searchRange.length == 3 ? searchRange[1] : searchRange[0];
		Integer zRange = searchRange.length == 3 ? searchRange[2] : searchRange[0];

		Integer xOffset = searchOffsets.length == 3 ? searchOffsets[0] : searchOffsets[0];
		Integer yOffset = searchOffsets.length == 3 ? searchOffsets[1] : searchOffsets[0];
		Integer zOffset = searchOffsets.length == 3 ? searchOffsets[2] : searchOffsets[0];

		for (int i = -xRange; i <= xRange; i++) {
			for (int k = -zRange; k <= zRange; k++) {
				for (int j = -yRange; j <= yRange; j++) {
					if (condition.isMatch(world, parent.posX + i + xOffset, parent.posY + j + yOffset, parent.posZ + k
							+ zOffset)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public String material(Material material) {
		if (material == Material.air) {
			return "air";
		} else if (material == Material.water) {
			return "water";
		} else if (material == Material.fire) {
			return "fire";
		} else if (material == Material.lava) {
			return "lava";
		} else if (material == Material.sand) {
			return "sand";
		} else if (material == Material.grass) {
			return "grass";
		} else if (material == Material.ground) {
			return "ground";
		} else if (material == Material.wood) {
			return "wood";
		} else if (material == Material.rock) {
			return "rock";
		} else if (material == Material.iron) {
			return "iron";
		} else if (material == Material.anvil) {
			return "anvil";
		} else if (material == Material.leaves) {
			return "leaves";
		} else if (material == Material.plants) {
			return "plants";
		} else if (material == Material.vine) {
			return "vine";
		} else if (material == Material.sponge) {
			return "sponge";
		} else if (material == Material.cloth) {
			return "cloth";
		} else if (material == Material.circuits) {
			return "circuits";
		} else if (material == Material.carpet) {
			return "carpet";
		} else if (material == Material.glass) {
			return "glass";
		} else if (material == Material.redstoneLight) {
			return "redstoneLight";
		} else if (material == Material.tnt) {
			return "tnt";
		} else if (material == Material.coral) {
			return "coral";
		} else if (material == Material.ice) {
			return "ice";
		} else if (material == Material.packedIce) {
			return "packedIce";
		} else if (material == Material.snow) {
			return "snow";
		} else if (material == Material.web) {
			return "web";
		} else if (material == Material.craftedSnow) {
			return "craftedSnow";
		} else if (material == Material.cactus) {
			return "cactus";
		} else if (material == Material.clay) {
			return "clay";
		} else if (material == Material.gourd) {
			return "gourd";
		} else if (material == Material.portal) {
			return "portal";
		} else if (material == Material.dragonEgg) {
			return "dragonEgg";
		} else if (material == Material.cake) {
			return "cake";
		} else if (material == Material.piston) {
			return "piston";
		} else {
			throw new IllegalArgumentException(String.format("Unknown material type %s", material));
		}
	}


	public int rand(int value) {
		return world.rand.nextInt(value);
	}
	
	public void log(String string) {
		JASLog.log().info("[TAG_LOG]".concat(string));
	}
}
