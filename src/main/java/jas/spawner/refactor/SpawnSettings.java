package jas.spawner.refactor;

import jas.common.JustAnotherSpawner;
import jas.spawner.refactor.configloader.ConfigLoader;

import java.io.File;

import net.minecraft.world.World;

/**
 * Contains all WorldSpecific spawn settings. Multiple instances may exist per world; there is a default implementation
 * for all dimensions but each dimension is capable of having its own override
 */
public class SpawnSettings {
	private BiomeSpawnLists biomeGroupRegistry;

	// private LivingGroupRegistry livingGroupRegistry;
	//
	// private CreatureTypeRegistry creatureTypeRegistry;
	// private LivingHandlerRegistry livingHandlerRegistry;
	// private BiomeSpawnListRegistry biomeSpawnListRegistry;
	//
	// private StructureHandlerRegistry structureHandlerRegistry;

	public SpawnSettings(World world, WorldProperties worldProperties, File settingsDirectory) {
		ConfigLoader loader = new ConfigLoader(settingsDirectory, worldProperties);
		biomeGroupRegistry = new BiomeSpawnLists(world, worldProperties, loader, JustAnotherSpawner.importedSpawnList());
	}

	public LivingHandlers livingHandlers() {
		return null; // biomeGroupRegistry.livinghandlers;
	}

	public LivingHandlers livingTypes() {
		return null; // biomeGroupRegistry.livingTypes;
	}
}