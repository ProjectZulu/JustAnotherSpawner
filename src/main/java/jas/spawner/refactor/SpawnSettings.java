package jas.spawner.refactor;

import jas.common.JustAnotherSpawner;
import jas.spawner.refactor.biome.BiomeAttributes;
import jas.spawner.refactor.biome.BiomeDictionaryGroups;
import jas.spawner.refactor.biome.BiomeGroups;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.entities.LivingAttributes;
import jas.spawner.refactor.entities.LivingMappings;

import java.io.File;

import net.minecraft.world.World;

/**
 * Contains all WorldSpecific spawn settings. Multiple instances may exist per world; there is a default implementation
 * for all dimensions but each dimension is capable of having its own override
 */
public class SpawnSettings {

	// private LivingGroupRegistry livingGroupRegistry;
	//
	// private CreatureTypeRegistry creatureTypeRegistry;
	// private LivingHandlerRegistry livingHandlerRegistry;
	// private BiomeSpawnListRegistry biomeSpawnListRegistry;
	//
	// private StructureHandlerRegistry structureHandlerRegistry;

	private LivingTypes livingTypes;

	private BiomeMappings biomeMappings;
	private BiomeDictionaryGroups dictionaryGroups;
	private BiomeAttributes biomeAttributes;
	private BiomeGroups biomeGroups;

	private LivingMappings livingMappings;
	private LivingAttributes livingAttributes;
	private LivingHandlers livingHandlers;

	private BiomeSpawnLists biomeGroupRegistry;

	public SpawnSettings(World world, WorldProperties worldProperties, File settingsDirectory) {
		ConfigLoader loader = new ConfigLoader(settingsDirectory, worldProperties);

		livingTypes = new LivingTypes();
		biomeMappings = new BiomeMappings(loader);
		dictionaryGroups = new BiomeDictionaryGroups(biomeMappings);
		biomeAttributes = new BiomeAttributes(loader, biomeMappings, dictionaryGroups);
		biomeGroups = new BiomeGroups(loader, biomeMappings, dictionaryGroups, biomeAttributes);

		livingMappings = new LivingMappings(loader);
		livingAttributes = new LivingAttributes(loader, livingMappings);
		livingHandlers = new LivingHandlers(livingMappings, livingAttributes);

		biomeGroupRegistry = new BiomeSpawnLists(world, worldProperties, livingMappings, loader,
				JustAnotherSpawner.importedSpawnList());
	}

	public BiomeSpawnLists biomeGroupRegistry() {
		return biomeGroupRegistry;
	}

	// public LivingHandlers livingHandlers() {
	// return null; // biomeGroupRegistry.livinghandlers;
	// }
	//
	// public LivingHandlers livingTypes() {
	// return null; // biomeGroupRegistry.livingTypes;
	// }
}