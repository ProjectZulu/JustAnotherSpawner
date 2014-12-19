package jas.spawner.modern;

import jas.api.CompatibilityRegistrationEvent;
import jas.common.JustAnotherSpawner;
import jas.common.Profile;
import jas.common.global.BiomeBlacklist;
import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.command.CommandJAS;
import jas.spawner.modern.spawner.ChunkSpawner;
import jas.spawner.modern.spawner.SpawnerTicker;
import jas.spawner.modern.world.WorldSettings;

import java.io.File;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class MVELProfile implements Profile {
	private static WorldSettings worldSettings;
	private static BiomeBlacklist biomeBlacklist;
	private static ImportedSpawnList importedSpawnList;

	public MVELProfile(BiomeBlacklist biomeBlacklist, ImportedSpawnList importedSpawnList) {
		this.biomeBlacklist = biomeBlacklist;
		this.importedSpawnList = importedSpawnList;
	}

	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(new EntityDespawner());
		MinecraftForge.TERRAIN_GEN_BUS.register(new ChunkSpawner(biomeBlacklist));
		FMLCommonHandler.instance().bus().register(new SpawnerTicker(biomeBlacklist));
		MinecraftForge.EVENT_BUS.post(new CompatibilityRegistrationEvent(new CompatabilityRegister()));
	}

	@Override
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandJAS(biomeBlacklist));
		loadFromConfig(JustAnotherSpawner.getModConfigDirectory(), event.getServer().worldServers[0]);
	}

	@Override
	public void loadFromConfig(File modConfigDirectoryFile, World server) {
		worldSettings = new WorldSettings(modConfigDirectoryFile, server, importedSpawnList);
	}

	@Override
	public void saveToConfig(File configDirectory, World world) {
		worldSettings.saveWorldSettings(configDirectory, world);
	}

	public static ImportedSpawnList importedSpawnList() {
		return importedSpawnList;
	}

	public static BiomeBlacklist biomeBlacklist() {
		return biomeBlacklist;
	}

	public static WorldSettings worldSettings() {
		return worldSettings;
	}
}
