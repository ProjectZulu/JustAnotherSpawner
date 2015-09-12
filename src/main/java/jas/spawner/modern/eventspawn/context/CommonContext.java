package jas.spawner.modern.eventspawn.context;

import jas.spawner.modern.spawner.FunctionsObjective;
import jas.spawner.modern.spawner.FunctionsTime;
import jas.spawner.modern.spawner.FunctionsLegacy;
import jas.spawner.modern.spawner.TagsSearch;
import jas.spawner.modern.spawner.TagsSearch.FunctionsSearch;
import jas.spawner.modern.spawner.FunctionsUtility;
import jas.spawner.modern.spawner.WorldAccessor;
import jas.spawner.modern.spawner.tags.Context;
import jas.spawner.modern.spawner.tags.TagsCount;
import jas.spawner.modern.spawner.tags.TagsLegacy;
import jas.spawner.modern.spawner.tags.TagsObjective;
import jas.spawner.modern.spawner.tags.TagsTime;
import jas.spawner.modern.spawner.tags.TagsUtility;
import jas.spawner.modern.spawner.tags.TagsWorld;
import net.minecraft.world.World;

public abstract class CommonContext implements Context {
	protected World world;

	public final int posX;
	public final int posY;
	public final int posZ;

	public final FunctionsObjective obj;
	public final FunctionsLegacy lgcy;
	public final FunctionsUtility util;
	public final WorldAccessor wrld;
	public final TagsTime time;
	public final TagsSearch search;

	public CommonContext(World world, int posX, int posY, int posZ) {
		this.world = world;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		obj = new FunctionsObjective(world, this);
		lgcy = new FunctionsLegacy(world, this);
		util = new FunctionsUtility(world, this);
		wrld = new WorldAccessor(world);
		time = new FunctionsTime(world);
		search = new FunctionsSearch(world, this);
	}

	@Override
	public int posX() {
		return posX;
	}

	@Override
	public int posY() {
		return posY;
	}

	@Override
	public int posZ() {
		return posZ;
	}

	@Override
	public TagsObjective obj() {
		return obj;
	}

	@Override
	public TagsUtility util() {
		return util;
	}

	@Override
	public TagsLegacy lgcy() {
		return lgcy;
	}

	@Override
	public TagsWorld wrld() {
		return wrld;
	}

	@Override
	public TagsTime time() {
		return time;
	}

	@Override
	public TagsSearch search() {
		return search;
	}
}
