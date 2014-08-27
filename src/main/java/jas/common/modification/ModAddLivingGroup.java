package jas.common.modification;

import jas.common.ImportedSpawnList;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
import jas.common.spawner.biome.structure.StructureHandler;
import jas.common.spawner.biome.structure.StructureHandlerRegistry;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.entry.SpawnListEntryBuilder;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerBuilder;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.EntityLiving;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ModAddLivingGroup extends BaseModification {

	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;
	private Optional<LivingGroup> livingGroup = Optional.absent();

	public ModAddLivingGroup(String groupName, ArrayList<String> contents) {
		this(groupName, null, contents);
	}

	public ModAddLivingGroup(String groupName, String configName, ArrayList<String> contents) {
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(LivingGroupRegistry registry) {
		LivingGroup group;
		if (configName.isPresent()) {
			group = new LivingGroup(groupName, configName.get(), contents);
		} else {
			group = new LivingGroup(groupName, contents);
		}
		if (registry.addLivingGroup(group)) {
			livingGroup = Optional.of(group);
		}
	}

	@Override
	public void applyModification(LivingHandlerRegistry registry) {
		if (livingGroup.isPresent()) {
			registry.addLivingHandler(new LivingHandlerBuilder(groupName));
		}
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
		if (livingGroup.isPresent() && !livingGroup.get().contents().isEmpty()) {
			BiomeGroupRegistry biomeGroupRegistry = registry.biomeGroupRegistry;
			LivingGroupRegistry livingGroupRegistry = registry.livingGroupRegistry;
			LivingHandlerRegistry livingHandlerRegistry = registry.livingHandlerRegistry;
			LivingHandler handler = livingHandlerRegistry.getLivingHandler(livingGroup.get().groupID);

			ImportedSpawnList importedSpawnList = JustAnotherSpawner.importedSpawnList();
			if (!handler.creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE) && handler.shouldSpawn) {
				for (BiomeGroup biomeGroup : biomeGroupRegistry.iDToGroup().values()) {
					SpawnListEntryBuilder spawnListEntry = findVanillaSpawnListEntry(biomeGroup, livingGroup.get(),
							importedSpawnList, biomeGroupRegistry, livingGroupRegistry);
					registry.addSpawnListEntry(spawnListEntry);
				}

			}
		}
	}

	@Override
	public void applyModification(StructureHandlerRegistry registry) {
		// Newly declared entities are not added to StructureSpawnList
	}

	private SpawnListEntryBuilder findVanillaSpawnListEntry(BiomeGroup group, LivingGroup livingGroup,
			ImportedSpawnList importedSpawnList, BiomeGroupRegistry biomeGroupRegistry,
			LivingGroupRegistry livingGroupRegistry) {
		for (String pckgNames : group.getBiomeNames()) {
			for (Integer biomeID : biomeGroupRegistry.pckgNameToBiomeID().get(pckgNames)) {
				Collection<net.minecraft.world.biome.BiomeGenBase.SpawnListEntry> spawnListEntries = importedSpawnList
						.getSpawnableCreatureList(biomeID);
				for (String jasName : livingGroup.entityJASNames()) {
					Class<? extends EntityLiving> livingClass = livingGroupRegistry.JASNametoEntityClass.get(jasName);
					for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : spawnListEntries) {
						if (spawnListEntry.entityClass.equals(livingClass)) {
							return new SpawnListEntryBuilder(livingGroup.groupID, group.groupID)
									.setWeight(spawnListEntry.itemWeight).setMinChunkPack(spawnListEntry.minGroupCount)
									.setMaxChunkPack(spawnListEntry.maxGroupCount);
						}
					}
				}
			}
		}
		return new SpawnListEntryBuilder(livingGroup.groupID, group.groupID);
	}
}
