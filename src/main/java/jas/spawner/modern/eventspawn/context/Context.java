package jas.spawner.modern.eventspawn.context;

import jas.spawner.modern.eventspawn.SpawnBuilder;

public interface Context {
	public SpawnBuilder spawn(String entityMapping);
}
