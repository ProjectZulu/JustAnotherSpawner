package jas.spawner.modern.modification;

import jas.common.JustAnotherSpawner;
import jas.spawner.modern.DefaultProps;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.world.WorldSettings;

import java.io.File;

import net.minecraft.world.World;

public class ModLoadConfig extends BaseModification {
	private File modConfigDirectoryFile;
	private World world;

	public ModLoadConfig(File modConfigDirectoryFile, World world) {
		this.modConfigDirectoryFile = modConfigDirectoryFile;
		this.world = world;
	}

	@Override
	public void applyModification(WorldSettings worldSettings) {
		worldSettings.loadWorldSettings(modConfigDirectoryFile, world);
	}
}
