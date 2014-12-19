package jas.modern.profile;

import jas.api.CompatibilityRegistrationEvent;
import jas.legacy.CompatabilityRegister;
import jas.legacy.EntityDespawner;
import jas.legacy.command.CommandJAS;
import jas.legacy.spawner.ChunkSpawner;
import jas.legacy.spawner.SpawnerTicker;
import jas.legacy.world.WorldSettings;
import jas.modern.BiomeBlacklist;
import jas.modern.ImportedSpawnList;
import jas.modern.JustAnotherSpawner;

import java.io.File;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class TAGProfile implements Profile {
	private static WorldSettings worldSettings;
	private static BiomeBlacklist biomeBlacklist;
	private static ImportedSpawnList importedSpawnList;

	public TAGProfile(BiomeBlacklist biomeBlacklist, ImportedSpawnList importedSpawnList) {
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
		event.registerServerCommand(new CommandJAS());
		loadFromConfig(JustAnotherSpawner.getModConfigDirectory(), event.getServer().worldServers[0]);
	}

	@Override
	public void loadFromConfig(File configDirectory, World world) {
		worldSettings = new WorldSettings(configDirectory, world, importedSpawnList);
	}

	@Override
	public void saveToConfig(File configDirectory, World world) {
		worldSettings.saveWorldSettings(configDirectory, world);
	}

	public ImportedSpawnList importedSpawnList() {
		return importedSpawnList;
	}

	public static BiomeBlacklist biomeBlacklist() {
		return biomeBlacklist;
	}

	public static WorldSettings worldSettings() {
		return worldSettings;
	}
}
