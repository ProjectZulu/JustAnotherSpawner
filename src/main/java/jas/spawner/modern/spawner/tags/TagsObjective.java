package jas.spawner.modern.spawner.tags;

public interface TagsObjective {
	public String block();

	public int light();

	public int torchlight();

	public boolean sky();
	
	public boolean ground();
	
	public int origin();

	public String material();

	public int difficulty();

	public int highestResistentBlock();

	public int playersInRange(int minRange, int maxRange);

	public int countEntitiesInRange(String[] searchNames, int minRange, int maxRange);

	public int countJASEntitiesInRange(String[] searchNames, int minRange, int maxRange);
}
