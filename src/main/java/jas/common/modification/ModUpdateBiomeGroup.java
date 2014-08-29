package jas.common.modification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.entity.EntityLiving;

import com.google.common.base.Optional;

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

public class ModUpdateBiomeGroup extends BaseModification {
	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;
	private String prevBiomeGroupId;

	public ModUpdateBiomeGroup(String groupName, ArrayList<String> contents) {
		this(groupName, groupName, null, contents);
	}

	public ModUpdateBiomeGroup(String prevBiomeGroupId, String groupName, String configName, ArrayList<String> contents) {
		this.prevBiomeGroupId = prevBiomeGroupId;
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
		if (configName.isPresent()) {
			registry.updateBiomeGroup(prevBiomeGroupId, groupName, configName.get(), contents);
		} else {
			registry.updateBiomeGroup(prevBiomeGroupId, groupName, contents);
		}
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
		// If GroupID has changed or the group is now empty (invalid)
		if (!prevBiomeGroupId.equalsIgnoreCase(groupName) || contents.isEmpty()) {
			LivingHandlerRegistry livingHandlerRegistry = registry.livingHandlerRegistry;
			BiomeGroupRegistry biomeGroupRegistry = registry.biomeGroupRegistry;
			LivingGroupRegistry livingGroupRegistry = registry.livingGroupRegistry;

			ImportedSpawnList importedSpawnList = JustAnotherSpawner.importedSpawnList();

			/* For all LivingGroups (that are not CreatureType NONE) */
			Collection<LivingHandler> livingHandlers = livingHandlerRegistry.getLivingHandlers();
			Collection<SpawnListEntryBuilder> spawnListToAdd = new ArrayList<SpawnListEntryBuilder>();
			for (LivingHandler handler : livingHandlers) {
				if (handler.creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE)) {
					continue;
				}
				registry.removeSpawnListEntry(handler.livingID, prevBiomeGroupId);
				// If BiomeGroup is empty there are no SpawnListEntries genereated for it
				if (!contents.isEmpty()) {
					BiomeGroup group = biomeGroupRegistry.getBiomeGroup(groupName);
					SpawnListEntryBuilder spawnListEntry = findVanillaSpawnListEntry(group, handler, importedSpawnList,
							biomeGroupRegistry, livingGroupRegistry);
					spawnListToAdd.add(spawnListEntry);
				}
			}
			registry.addSpawnListEntry(spawnListToAdd);
		}
	}

	private SpawnListEntryBuilder findVanillaSpawnListEntry(BiomeGroup group, LivingHandler livingHandler,
			ImportedSpawnList importedSpawnList, BiomeGroupRegistry biomeGroupRegistry,
			LivingGroupRegistry livingGroupRegistry) {
		for (String pckgNames : group.getBiomeNames()) {
			for (Integer biomeID : biomeGroupRegistry.pckgNameToBiomeID().get(pckgNames)) {
				Collection<net.minecraft.world.biome.BiomeGenBase.SpawnListEntry> spawnListEntries = importedSpawnList
						.getSpawnableCreatureList(biomeID);
				for (String jasName : livingHandler.namedJASSpawnables) {
					Class<? extends EntityLiving> livingClass = livingGroupRegistry.JASNametoEntityClass.get(jasName);
					for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : spawnListEntries) {
						if (spawnListEntry.entityClass.equals(livingClass)) {
							return new SpawnListEntryBuilder(livingHandler.livingID, group.groupID)
									.setWeight(spawnListEntry.itemWeight).setMinChunkPack(spawnListEntry.minGroupCount)
									.setMaxChunkPack(spawnListEntry.maxGroupCount);
						}
					}
				}
			}
		}
		return new SpawnListEntryBuilder(livingHandler.livingID, group.groupID);
	}
}
