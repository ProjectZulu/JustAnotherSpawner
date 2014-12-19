package jas.modern.modification;

import java.util.EnumSet;

import jas.modern.WorldSettings;
import jas.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.modern.spawner.biome.structure.StructureHandlerRegistry;
import jas.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.type.CreatureTypeRegistry;

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