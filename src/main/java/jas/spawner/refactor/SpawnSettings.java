package jas.spawner.refactor;

import jas.common.JustAnotherSpawner;
import jas.common.global.ImportedSpawnList;
import jas.spawner.refactor.biome.BiomeAttributes;
import jas.spawner.refactor.biome.BiomeDictionaryGroups;
import jas.spawner.refactor.biome.BiomeGroupBuilder;
import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.biome.BiomeGroups;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.configsloader.BiomeSettingsLoader;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.configsloader.LivingSettingsLoader;
import jas.spawner.refactor.despawn.DespawnRules;
import jas.spawner.refactor.entities.LivingAttributes;
import jas.spawner.refactor.entities.LivingGroupBuilder;
import jas.spawner.refactor.entities.LivingGroupBuilder.LivingGroup;
import jas.spawner.refactor.entities.LivingMappings;
import jas.spawner.refactor.structure.StructureHandlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.world.World;

/**
 * Contains all WorldSpecific spawn settings. Multiple instances may exist per world; there is a default implementation
 * for all dimensions but each dimension is capable of having its own override
 */
public class SpawnSettings {
	private LivingTypes livingTypes;
	private DespawnRules despawnRules;

	private BiomeSettings biomeSettings;
	private LivingSettings livingSettings;

	private StructureHandlers structureHandlers;
	private BiomeSpawnLists biomeGroupRegistry;

	public static class BiomeSettings {
		private BiomeMappings biomeMappings;
		private BiomeDictionaryGroups dictionaryGroups;
		private BiomeAttributes biomeAttributes;
		private BiomeGroups biomeGroups;

		private BiomeSettings(ConfigLoader loader) {
			this.biomeMappings = new BiomeMappings(loader);
			this.dictionaryGroups = new BiomeDictionaryGroups(biomeMappings);
			this.biomeAttributes = new BiomeAttributes(loader, biomeMappings, dictionaryGroups);
			this.biomeGroups = new BiomeGroups(loader, biomeMappings, dictionaryGroups, biomeAttributes);
		}

		public void saveToConfig(ConfigLoader loader) {
			Collection<BiomeGroupBuilder> attributeGroupsBuilders = new ArrayList<BiomeGroupBuilder>();
			for (BiomeGroup biomeGroup : biomeAttributes.iDToGroup().values()) {
				attributeGroupsBuilders.add(new BiomeGroupBuilder(biomeGroup));
			}
			Collection<BiomeGroupBuilder> biomeGroupBuilders = new ArrayList<BiomeGroupBuilder>();
			for (BiomeGroup biomeGroup : this.biomeGroups.iDToGroup().values()) {
				biomeGroupBuilders.add(new BiomeGroupBuilder(biomeGroup));
			}
			loader.biomeGroupLoader = new LoadedFile(new BiomeSettingsLoader(biomeMappings.keyToMapping(),
					attributeGroupsBuilders, biomeGroupBuilders));
		}

		public BiomeMappings biomeMappings() {
			return biomeMappings;
		}

		public BiomeDictionaryGroups dictionaryGroups() {
			return dictionaryGroups;
		}

		public BiomeAttributes biomeAttributes() {
			return biomeAttributes;
		}

		public BiomeGroups biomeGroups() {
			return biomeGroups;
		}
	}

	public static class LivingSettings {
		private LivingMappings livingMappings;
		private LivingAttributes livingAttributes;
		private LivingHandlers livingHandlers;

		private LivingSettings(ConfigLoader loader, ImportedSpawnList importedSpawnList) {
			this.livingMappings = new LivingMappings(loader);
			this.livingAttributes = new LivingAttributes(loader, livingMappings);
			this.livingHandlers = new LivingHandlers(loader, livingMappings, livingAttributes);
		}

		public void saveToConfig(WorldProperties worldProperties, ConfigLoader loader) {
			Collection<LivingGroupBuilder> livingGroupBuilders = new ArrayList<LivingGroupBuilder>();
			for (LivingGroup livingGroup : livingAttributes.iDToGroup().values()) {
				livingGroupBuilders.add(new LivingGroupBuilder(livingGroup));
			}
			loader.livingGroupLoader = new LoadedFile(new LivingSettingsLoader(livingMappings.keyToMapping(),
					livingGroupBuilders));
			livingHandlers.saveToConfig(worldProperties, loader, livingMappings, livingAttributes);
		}

		public LivingMappings livingMappings() {
			return livingMappings;
		}

		public LivingAttributes livingAttributes() {
			return livingAttributes;
		}

		public LivingHandlers livingHandlers() {
			return livingHandlers;
		}
	}

	public SpawnSettings(World world, WorldProperties worldProperties, File settingsDirectory) {
		loadFromConfig(world, worldProperties, settingsDirectory);
	}

	public void loadFromConfig(World world, WorldProperties worldProperties, File settingsDirectory) {
		ConfigLoader loader = new ConfigLoader(settingsDirectory, worldProperties);
		this.livingTypes = new LivingTypes(loader);
		this.despawnRules = new DespawnRules(loader, livingSettings, JustAnotherSpawner.importedSpawnList());
		this.biomeSettings = new BiomeSettings(loader);
		this.livingSettings = new LivingSettings(loader, JustAnotherSpawner.importedSpawnList());
		this.structureHandlers = new StructureHandlers(world, loader, livingSettings);
		this.biomeGroupRegistry = new BiomeSpawnLists(world, loader, worldProperties, livingSettings, biomeSettings,
				livingTypes, JustAnotherSpawner.importedSpawnList());
	}

	public void saveToConfig(World world, WorldProperties worldProperties, File settingsDirectory) {
		ConfigLoader loader = new ConfigLoader(settingsDirectory, worldProperties);
		this.livingTypes.saveToConfig(loader);
		this.despawnRules.saveToConfig(loader);
		this.biomeSettings.saveToConfig(loader);
		this.livingSettings.saveToConfig(worldProperties, loader);
		this.structureHandlers.saveToConfig(worldProperties, loader);
		this.biomeGroupRegistry.saveToConfig(worldProperties, loader);
	}
	
	public LivingTypes livingTypes() {
		return livingTypes;
	}

	public DespawnRules despawnRules() {
		return despawnRules;
	}

	public BiomeMappings biomeMappings() {
		return biomeSettings.biomeMappings;
	}

	public BiomeDictionaryGroups dictionaryGroups() {
		return biomeSettings.dictionaryGroups;
	}

	public BiomeAttributes biomeAttributes() {
		return biomeSettings.biomeAttributes;
	}

	public BiomeGroups biomeGroups() {
		return biomeSettings.biomeGroups;
	}

	public LivingMappings livingMappings() {
		return livingSettings.livingMappings;
	}

	public LivingAttributes livingAttributes() {
		return livingSettings.livingAttributes;
	}

	public LivingHandlers livingHandlers() {
		return livingSettings.livingHandlers;
	}

	public StructureHandlers structureHandlers() {
		return structureHandlers;
	}

	public BiomeSpawnLists biomeGroupRegistry() {
		return biomeGroupRegistry;
	}

	public BiomeSettings biomeSettings() {
		return biomeSettings;
	}

	public LivingSettings livingSettings() {
		return livingSettings;
	}
}