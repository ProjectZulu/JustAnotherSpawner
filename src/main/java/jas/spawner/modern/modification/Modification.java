package jas.spawner.modern.modification;

import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.biome.structure.StructureHandlerRegistry;
import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.type.CreatureTypeRegistry;
import jas.spawner.modern.world.WorldSettings;

/**
 * Interface for making changes to the spawn database structures. Must be called in the order implied by the
 * dependencies i.e. BiomeGroupRegistry -> LivingGroupRegistry -> CreatureTypeRegistry -> LivingHandlerRegistry ->
 * StructureHandlerRegistry -> BiomeSpawnListRegistry.
 * 
 * TODO A method to fetch all instances i.e. passSources(WorldSettings worldSettings, BiomeGroupRegistry registry,
 * LivingGroupRegistry registry, etc.)
 */
public interface Modification {
	public void applyModification(WorldSettings worldSettings);

	public void applyModification(BiomeGroupRegistry registry);

	public void applyModification(LivingGroupRegistry registry);

	public void applyModification(CreatureTypeRegistry registry);

	public void applyModification(LivingHandlerRegistry registry);

	public void applyModification(StructureHandlerRegistry registry);

	public void applyModification(BiomeSpawnListRegistry registry);
}