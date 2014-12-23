package jas.spawner.refactor;

import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.DefaultProps;

import java.io.File;
import java.util.HashMap;

import net.minecraft.world.World;

public class WorldSettings {
	private WorldProperties worldProperties;

	SpawnSettings defaultSpawnSettings;
	private HashMap<Integer, SpawnSettings> dimSpawnOverrides;

	public WorldSettings(File modConfigDirectoryFile, World world, ImportedSpawnList importedSpawnList) {
		worldProperties = new WorldProperties();
		loadWorldSettings(modConfigDirectoryFile, world);
		saveWorldSettings(modConfigDirectoryFile, world);
	}

	public void loadWorldSettings(File modConfigDirectoryFile, World world) {
		File defaultWorldSettingDirectory = new File(DefaultProps.WORLDSETTINGSDIR
				+ worldProperties.getFolderConfiguration().saveName + "/");
		defaultSpawnSettings = new SpawnSettings(world, worldProperties, defaultWorldSettingDirectory);
	}

	public void saveWorldSettings(File modConfigDirectoryFile, World world) {

	}

	public SpawnSettings getSpawnSettings(World world) {
		if (dimSpawnOverrides.isEmpty()) {
			return defaultSpawnSettings;
		} else {
			SpawnSettings dimSpawnSettings = dimSpawnOverrides.get(world.provider.dimensionId);
			return dimSpawnSettings != null ? dimSpawnSettings : defaultSpawnSettings;
		}
	}
}
