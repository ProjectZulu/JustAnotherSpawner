package jas.spawner.modern.spawner.tags;

public interface TagsLegacy {
	public boolean height(int minHeight, int maxHeight);

	public boolean light(int minLight, int maxLight);

	public boolean torchlight(int minLight, int maxLight);

	public boolean origin(int minDistance, int maxDistance);

	public boolean top();

	public boolean filler();

	public boolean dimension(int dimension);

	public boolean location(int[] offset, int[] range);

	public boolean players(int[] searchRange, int[] minMaxBounds);

	public boolean entities(String[] searchNames, int[] searchRange, int[] minMaxBounds);

	public boolean random(int range, int offset, int maxValue);
	
	public boolean difficulty(int desiredDifficulty);
}
