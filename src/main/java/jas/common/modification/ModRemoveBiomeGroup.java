package jas.common.modification;

import java.util.Collection;

import jas.common.ImportedSpawnList;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.entry.SpawnListEntryBuilder;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public class ModRemoveBiomeGroup extends BaseModification {

	public final String groupName;

	public ModRemoveBiomeGroup(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
		registry.removeBiomeGroup(groupName);
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
		LivingHandlerRegistry livingHandlerRegistry = registry.livingHandlerRegistry;
		BiomeGroupRegistry biomeGroupRegistry = registry.biomeGroupRegistry;

		/* For all LivingGroups (that are not CreatureType NONE) */
		Collection<LivingHandler> livingHandlers = livingHandlerRegistry.getLivingHandlers();
		for (LivingHandler handler : livingHandlers) {
			if (handler.creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE)) {
				continue;
			}
			registry.removeSpawnListEntry(handler.livingID, groupName);
		}
	}
}
