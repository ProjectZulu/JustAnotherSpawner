package jas.spawner.modern.eventspawn;

import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry;
import net.minecraft.world.World;

public interface SpawnBuilder {	
	
	public SpawnBuilder offset(int radius);
	
	public SpawnBuilder offset(double offsetX, double offsetY, double offsetZ);
	
	public void spawn(World world, LivingGroupRegistry groupRegistry);
}
