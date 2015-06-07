package jas.spawner.modern.eventspawn.context;

import jas.spawner.modern.spawner.LegacyTags;
import jas.spawner.modern.spawner.TagsObjective;
import jas.spawner.modern.spawner.TagsUtility;
import jas.spawner.modern.spawner.TimeHelper;
import jas.spawner.modern.spawner.WorldAccessor;
import jas.spawner.modern.spawner.tags.BaseFunctions;
import jas.spawner.modern.spawner.tags.CountFunctions;
import jas.spawner.modern.spawner.tags.LegacyFunctions;
import jas.spawner.modern.spawner.tags.ObjectiveFunctions;
import jas.spawner.modern.spawner.tags.TimeFunctions;
import jas.spawner.modern.spawner.tags.UtilityFunctions;
import jas.spawner.modern.spawner.tags.WorldFunctions;
import net.minecraft.world.World;

public abstract class CommonContext implements Context, BaseFunctions {
	private World world;

	public final int posX;
	public final int posY;
	public final int posZ;

	public final TagsObjective obj;
	public final LegacyTags lgcy;
	public final TagsUtility util;
	public final WorldAccessor wrld;
	public final TimeFunctions time;

	public CommonContext(World world, int posX, int posY, int posZ) {
		this.world = world;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		obj = new TagsObjective(world, this);
		lgcy = new LegacyTags(world, this);
		util = new TagsUtility(world, this);
		wrld = new WorldAccessor(world);
		time = new TimeHelper(world);
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
	public ObjectiveFunctions obj() {
		return obj;
	}

	@Override
	public UtilityFunctions util() {
		return util;
	}

	@Override
	public LegacyFunctions lgcy() {
		return lgcy;
	}

	@Override
	public WorldFunctions wrld() {
		return wrld;
	}

	@Override
	public CountFunctions count() {
		throw new UnsupportedOperationException("Count Functions are not supported for SpawnEvent objects");
	}

	@Override
	public TimeFunctions time() {
		return time;
	}
}
