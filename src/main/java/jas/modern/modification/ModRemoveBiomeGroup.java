package jas.modern.modification;

import java.util.Collection;

import jas.modern.ImportedSpawnList;
import jas.modern.JustAnotherSpawner;
import jas.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.modern.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
import jas.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.modern.spawner.creature.entry.SpawnListEntryBuilder;
import jas.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.modern.spawner.creature.handler.LivingHandler;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.modern.spawner.creature.type.CreatureTypeRegistry;

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
