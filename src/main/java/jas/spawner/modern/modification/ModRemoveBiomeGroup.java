package jas.spawner.modern.modification;

import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandler;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.type.CreatureTypeRegistry;

import java.util.Collection;

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
