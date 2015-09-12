package jas.spawner.modern.spawner.tags;

public interface TagsCount {
	public int getLocalEntityTypeCount(String entityType);

	public int getLocalEntityClassCount(String entityJasName);

	public int getGlobalEntityTypeCount(String entityType);

	public int getGlobalEntityClassCount(String entityJasName);

	public int entitiesSpawnedThisChunk();

	public int entitiesSpawnedThisPack();

	public int clodCount(String entityType);
}
