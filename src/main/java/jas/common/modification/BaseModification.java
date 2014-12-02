package jas.common.modification;

import jas.common.WorldSettings;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.structure.StructureHandlerRegistry;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

/**
 * Convenience class to allow subclasses to only override a registry they need
 * to change
 */
public class BaseModification implements Modification {

	@Override
	public void applyModification(WorldSettings worldSettings) {
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
	}

	@Override
	public void applyModification(LivingGroupRegistry registry) {
	}

	@Override
	public void applyModification(CreatureTypeRegistry registry) {
	}

	@Override
	public void applyModification(LivingHandlerRegistry registry) {
	}

	@Override
	public void applyModification(StructureHandlerRegistry registry) {
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
	}
}
