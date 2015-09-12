package jas.spawner.modern.spawner.tags;

import jas.spawner.modern.spawner.TagsSearch;

public interface Context {
	public int posX();

	public int posY();

	public int posZ();

	public TagsObjective obj();

	public TagsUtility util();

	public TagsLegacy lgcy();

	public TagsWorld wrld();

	public TagsTime time();

	public TagsSearch search();
}
