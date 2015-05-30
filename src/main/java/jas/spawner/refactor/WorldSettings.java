package jas.spawner.refactor;

import jas.common.global.ImportedSpawnList;

import java.io.File;
import java.util.HashMap;

import net.minecraft.world.World;

public class WorldSettings {
	private WorldProperties worldProperties;
	private SpawnSettings defaultSpawnSettings;
	private HashMap<Integer, SpawnSettings> dimSpawnOverrides;

	public WorldSettings(File profileDirectory, World world, ImportedSpawnList importedSpawnList) {
		this.worldProperties = new WorldProperties();
		this.loadWorldSettings(profileDirectory, world);
		this.saveWorldSettings(profileDirectory, world);
	}

	public void loadWorldSettings(File profileDirectory, World world) {
		File defaultWorldSettingDirectory = new File(profileDirectory
				+ worldProperties.getFolderConfiguration().saveName + "/");
		defaultSpawnSettings = new SpawnSettings(world, worldProperties, defaultWorldSettingDirectory);
	}

	public void saveWorldSettings(File profileDirectory, World world) {
		File defaultWorldSettingDirectory = new File(profileDirectory
				+ worldProperties.getFolderConfiguration().saveName + "/");
		defaultSpawnSettings.saveToConfig(world, worldProperties, defaultWorldSettingDirectory);
	}

	public SpawnSettings getSpawnSettings(World world) {
		if (dimSpawnOverrides.isEmpty()) {
			return defaultSpawnSettings;
		} else {
			SpawnSettings dimSpawnSettings = dimSpawnOverrides.get(world.provider.dimensionId);
			return dimSpawnSettings != null ? dimSpawnSettings : defaultSpawnSettings;
		}
	}

	public BiomeSpawnLists getBiomeSpawnListRegistry(World world) {
		return defaultSpawnSettings.biomeGroupRegistry();
	}
}
