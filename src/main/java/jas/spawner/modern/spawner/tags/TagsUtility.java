package jas.spawner.modern.spawner.tags;

import jas.spawner.modern.spawner.FunctionsUtility.Conditional;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public interface TagsUtility {
	public boolean inRange(int current, int minRange, int maxRange);

	public boolean searchAndEvaluateBlock(Conditional condition, Integer[] searchRange, Integer[] searchOffsets);

	public String material(Material material);

	public int rand(int value);
		
	public boolean blockFoot(String[] blockKeys);
	
	public boolean blockFoot(String[] blockKeys, Integer[] metas);
	
	public void log(String string);

}
