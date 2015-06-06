package jas.spawner.modern;

import jas.api.CompatibilityRegistrationEvent;
import jas.api.StructureInterpreter;
import jas.common.JustAnotherSpawner;
import jas.common.Profile;
import jas.common.global.BiomeBlacklist;
import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.command.CommandJAS;
import jas.spawner.modern.eventspawn.EventSpawnRegistry;
import jas.spawner.modern.eventspawn.EventSpawnTrigger;
import jas.spawner.modern.spawner.ChunkSpawner;
import jas.spawner.modern.spawner.SpawnerTicker;
import jas.spawner.modern.spawner.biome.structure.StructureInterpreterNether;
import jas.spawner.modern.spawner.biome.structure.StructureInterpreterOverworldStructures;
import jas.spawner.modern.spawner.biome.structure.StructureInterpreterSwamp;
import jas.spawner.modern.world.WorldSettings;

import java.io.File;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class MVELProfile implements Profile {
	private static WorldSettings worldSettings;
	private static BiomeBlacklist biomeBlacklist;
	private static ImportedSpawnList importedSpawnList;
	public static final String PROFILE_FOLDER = "Basic/";

	public MVELProfile(BiomeBlacklist biomeBlacklist, ImportedSpawnList importedSpawnList) {
		this.biomeBlacklist = biomeBlacklist;
		this.importedSpawnList = importedSpawnList;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(new EntityDespawner());
		EventSpawnTrigger spawnTrigger = new EventSpawnTrigger(this);
		MinecraftForge.EVENT_BUS.register(spawnTrigger);
		FMLCommonHandler.instance().bus().register(spawnTrigger);
		MinecraftForge.TERRAIN_GEN_BUS.register(new ChunkSpawner(biomeBlacklist));
		FMLCommonHandler.instance().bus().register(new SpawnerTicker(biomeBlacklist));
		MinecraftForge.EVENT_BUS.post(new CompatibilityRegistrationEvent(new CompatabilityRegister()));
	}

	@Override
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandJAS(biomeBlacklist));
		loadFromConfig(JustAnotherSpawner.getModConfigDirectory(), event.getServer().worldServers[0]);
		saveToConfig(JustAnotherSpawner.getModConfigDirectory(), event.getServer().worldServers[0]);
	}

	@Override
	public void loadFromConfig(File jasConfigDirectory, World server) {
		File profileDir = new File(jasConfigDirectory, DefaultProps.MODDIR + DefaultProps.WORLDSETTINGSDIR
				+ PROFILE_FOLDER);
		// This is part of Profile Specific directories, remove in 0.18
		if (!profileDir.exists()) {
			profileDir = new File(jasConfigDirectory, DefaultProps.MODDIR + DefaultProps.WORLDSETTINGSDIR);
		}
		worldSettings = new WorldSettings(profileDir, server, importedSpawnList);
	}

	@Override
	public void saveToConfig(File jasConfigDirectory, World world) {
		File profileDir = new File(jasConfigDirectory, DefaultProps.MODDIR + DefaultProps.WORLDSETTINGSDIR
				+ PROFILE_FOLDER);
//		profileDir = new File(jasConfigDirectory, DefaultProps.MODDIR + DefaultProps.WORLDSETTINGSDIR);
		worldSettings.saveWorldSettings(profileDir, world);
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

	@SubscribeEvent
	public void VanillaStructureCompataiblity(CompatibilityRegistrationEvent event) {
		StructureInterpreter overworld = new StructureInterpreterOverworldStructures();
		MinecraftForge.EVENT_BUS.register(overworld);
		event.loader.registerObject(overworld);

		StructureInterpreter swamp = new StructureInterpreterSwamp();
		MinecraftForge.EVENT_BUS.register(swamp);
		event.loader.registerObject(swamp);

		StructureInterpreter nether = new StructureInterpreterNether();
		MinecraftForge.EVENT_BUS.register(nether);
		event.loader.registerObject(nether);
	}
}
