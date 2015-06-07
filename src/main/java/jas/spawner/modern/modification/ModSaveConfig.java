package jas.spawner.modern.modification;

import jas.spawner.modern.world.WorldSettings;

import java.io.File;

import net.minecraft.world.World;

public class ModSaveConfig extends BaseModification {
	public final File configDirectory;
	public final World world;

	public ModSaveConfig(File configDirectory, World world) {
		this.configDirectory = configDirectory;
		this.world = world;
	}

	@Override
	public void applyModification(WorldSettings worldSettings) {
		worldSettings.saveWorldSettings(configDirectory, world);
	}
}
