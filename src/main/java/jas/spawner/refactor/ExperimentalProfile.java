package jas.spawner.refactor;

import jas.api.CompatibilityRegistrationEvent;
import jas.common.JustAnotherSpawner;
import jas.common.Profile;
import jas.common.global.BiomeBlacklist;
import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.DefaultProps;

import java.io.File;

import net.minecraft.world.World;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Experimental Profile. SpawnListEntry is the center spawning object, each entry specifies the corresponding
 * LivingHandler and LivingType
 * 
 * Notable issue is behavior of entities after changes to LivingType/LivingHandler name and how those do not propegate
 * to already spawned entities; or cannot do so with clumsily detailing defaults elsewhere OR removing the ability to
 * scriptify EntitySpawn expression (change to Array?).
 */
public class ExperimentalProfile implements Profile {
	private static WorldSettings worldSettings;
	private static BiomeBlacklist biomeBlacklist;
	private static ImportedSpawnList importedSpawnList;
	public static final String PROFILE_FOLDER = "EXPERIMENTAL/";

	@Override
	public void init() {
		// MinecraftForge.EVENT_BUS.register(new EntityDespawner());
		// MinecraftForge.TERRAIN_GEN_BUS.register(new ChunkSpawner(biomeBlacklist));
		// FMLCommonHandler.instance().bus().register(new SpawnerTicker(biomeBlacklist));
		// MinecraftForge.EVENT_BUS.post(new CompatibilityRegistrationEvent(new CompatabilityRegister()));
	}

	@Override
	public void serverStart(FMLServerStartingEvent event) {
		// event.registerServerCommand(new CommandJAS(biomeBlacklist));
		loadFromConfig(JustAnotherSpawner.getModConfigDirectory(), event.getServer().worldServers[0]);
	}

	@Override
	public void loadFromConfig(File configDirectory, World world) {
		File profileDir = new File(DefaultProps.WORLDSETTINGSDIR + PROFILE_FOLDER);
		worldSettings = new WorldSettings(profileDir, world, importedSpawnList);
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

	@SubscribeEvent
	public void VanillaStructureCompataiblity(CompatibilityRegistrationEvent event) {
		// StructureInterpreter overworld = new StructureInterpreterOverworldStructures();
		// MinecraftForge.EVENT_BUS.register(overworld);
		// event.loader.registerObject(overworld);
		//
		// StructureInterpreter swamp = new StructureInterpreterSwamp();
		// MinecraftForge.EVENT_BUS.register(swamp);
		// event.loader.registerObject(swamp);
		//
		// StructureInterpreter nether = new StructureInterpreterNether();
		// MinecraftForge.EVENT_BUS.register(nether);
		// event.loader.registerObject(nether);
	}
}
