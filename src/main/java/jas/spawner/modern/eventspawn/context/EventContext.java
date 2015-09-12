package jas.spawner.modern.eventspawn.context;

import net.minecraft.world.World;

public abstract class EventContext extends CommonContext implements Context {

	public EventContext(World world, int posX, int posY, int posZ) {
		super(world, posX, posY, posZ);
	}
}
