package jas.spawner.card;

import jas.api.CompatibilityRegistrationEvent;
import jas.common.JustAnotherSpawner;
import jas.common.Profile;
import jas.common.global.BiomeBlacklist;
import jas.common.global.ImportedSpawnList;
import jas.spawner.refactor.WorldSettings;

import java.io.File;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Spawner where Mappings specify a 'Card' (similar to LivingHanlder which specifies where the entity can spawn,
 * despawn, and other miscelaneous proeprties.
 */
public class CardProfile implements Profile {
	// private static WorldSettings worldSettings;
	private static BiomeBlacklist biomeBlacklist;
	private static ImportedSpawnList importedSpawnList;

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
		// worldSettings = new WorldSettings(configDirectory, world, importedSpawnList);
	}

	@Override
	public void saveToConfig(File configDirectory, World world) {
		// worldSettings.saveWorldSettings(configDirectory, world);
	}

	public static ImportedSpawnList importedSpawnList() {
		return importedSpawnList;
	}

	public static BiomeBlacklist biomeBlacklist() {
		return biomeBlacklist;
	}

	public static WorldSettings worldSettings() {
		return null;// worldSettings;
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
