package jas.common.modification;

import jas.common.WorldSettings;

import java.io.File;

import org.apache.logging.log4j.core.config.BaseConfiguration;

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
