package jas.common.modification;

import java.util.EnumSet;

import jas.common.WorldSettings;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.structure.StructureHandlerRegistry;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public interface Modification {
	public void applyModification(WorldSettings worldSettings);

	public void applyModification(BiomeGroupRegistry registry);

	public void applyModification(LivingGroupRegistry registry);

	public void applyModification(CreatureTypeRegistry registry);

	public void applyModification(LivingHandlerRegistry registry);

	public void applyModification(StructureHandlerRegistry registry);

	public void applyModification(BiomeSpawnListRegistry registry);
}