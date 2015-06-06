package jas.spawner.modern.spawner.tags;

import jas.spawner.modern.spawner.TagsUtility.Conditional;
import net.minecraft.block.material.Material;

public interface UtilityFunctions {
	public boolean inRange(int current, int minRange, int maxRange);

	public boolean searchAndEvaluateBlock(Conditional condition, Integer[] searchRange, Integer[] searchOffsets);

	public String material(Material material);

	public int rand(int value);

	public void log(String string);

}
