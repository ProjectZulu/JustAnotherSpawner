package jas.modern;

import jas.api.CompatibilityRegistrationEvent;
import jas.modern.command.CommandJAS;
import jas.modern.profile.MVELProfile;
import jas.modern.profile.Profile;
import jas.modern.profile.TAGProfile;
import jas.modern.proxy.CommonProxy;
import jas.modern.spawner.ChunkSpawner;
import jas.modern.spawner.SpawnerTicker;
import jas.modern.spawner.biome.structure.StructureInterpreterHelper;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.GameRules;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.google.gson.Gson;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = DefaultProps.MODID, name = DefaultProps.MODNAME, dependencies = "after:*", useMetadata = true)
public class JustAnotherSpawner {

    @Instance(DefaultProps.MODID)
    public static JustAnotherSpawner modInstance;

    @SidedProxy(clientSide = "jas.common.proxy.ClientProxy", serverSide = "jas.common.proxy.CommonProxy")
    public static CommonProxy proxy;

    /* Only Populated after {@link#FMLPreInitializationEvent} */
    private static File modConfigDirectoryFile;

    public static File getModConfigDirectory() {
        return modConfigDirectoryFile;
    }

	private static ImportedSpawnList importedSpawnList;

	public static ImportedSpawnList importedSpawnList() {
		return importedSpawnList;
	}
    
	BiomeBlacklist biomeBlacklist;

	public BiomeBlacklist biomeBlacklist() {
		return biomeBlacklist;
	}
    
    private static GlobalSettings globalSettings;

    public static GlobalSettings globalSettings() {
        return globalSettings;
    }

    private static WorldSettings worldSettings;

    public static WorldSettings worldSettings() {
        return worldSettings;
	}

	private static Profile currentProfile;

	public static Profile profile() {
		return currentProfile;
	}

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    	
        modConfigDirectoryFile = event.getModConfigurationDirectory();
        Gson gson = GsonHelper.createGson(true);

        File globalSettingsFile = new File(modConfigDirectoryFile, DefaultProps.MODDIR + "GlobalProperties.cfg");
        globalSettings = GsonHelper.readOrCreateFromGson(FileUtilities.createReader(globalSettingsFile, false),
                GlobalSettings.class, gson);
        GsonHelper.writeToGson(FileUtilities.createWriter(globalSettingsFile, true), globalSettings, gson);

        File loggingSettings = new File(modConfigDirectoryFile, DefaultProps.MODDIR + "LoggingProperties.cfg");
        JASLog jasLog = GsonHelper.readOrCreateFromGson(FileUtilities.createReader(loggingSettings, false), JASLog.class, gson);
        JASLog.setLogger(jasLog);
        GsonHelper.writeToGson(FileUtilities.createWriter(loggingSettings, true), jasLog, gson);

		if (globalSettings.spawningProfile.equals(GlobalSettings.profileTAGS)) {
			currentProfile = new TAGProfile();
		} else {
			currentProfile = new MVELProfile();
		}
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EntityDespawner());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        BiomeDictionary.registerAllBiomes();
        biomeBlacklist = new BiomeBlacklist(modConfigDirectoryFile);
        MinecraftForge.TERRAIN_GEN_BUS.register(new ChunkSpawner(biomeBlacklist));
        FMLCommonHandler.instance().bus().register(new SpawnerTicker(biomeBlacklist));
        importedSpawnList = new ImportedSpawnList(biomeBlacklist, globalSettings.emptyVanillaSpawnLists);
        MinecraftForge.EVENT_BUS.post(new CompatibilityRegistrationEvent(new CompatabilityRegister()));
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        worldSettings = new WorldSettings(modConfigDirectoryFile, event.getServer().worldServers[0], importedSpawnList);
        event.registerServerCommand(new CommandJAS(this));
		if (globalSettings.emptyVanillaSpawnLists) {
			JASLog.log().info("Removing Netherbridge spawnlist");
			ChunkProviderHell chunkProviderHell = StructureInterpreterHelper.getInnerChunkProvider(
					event.getServer().worldServers[1], ChunkProviderHell.class);
			if (chunkProviderHell != null) {
				List<?> netherSpawnList;
				MapGenNetherBridge genNetherBridge;
				try {
					genNetherBridge = ReflectionHelper.getCatchableFieldFromReflection("field_73172_c",
							chunkProviderHell, MapGenNetherBridge.class);
					netherSpawnList = ReflectionHelper.getCatchableFieldFromReflection("field_75060_e",
							genNetherBridge, List.class);
				} catch (NoSuchFieldException e) {
					genNetherBridge = ReflectionHelper.getFieldFromReflection("genNetherBridge", chunkProviderHell,
							MapGenNetherBridge.class);
					netherSpawnList = ReflectionHelper.getFieldFromReflection("spawnList", genNetherBridge, List.class);
				}
				Iterator<?> spawnIterator = netherSpawnList.iterator();
				while (spawnIterator.hasNext()) {
					Object spawn = spawnIterator.next();
					spawnIterator.remove();
				}
			}
		}
	}

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        GameRules gameRule = event.world.getGameRules();
        if (gameRule != null && globalSettings.turnGameruleSpawningOff) {
            JASLog.log().info("Setting GameRule doMobSpawning for %s-%s to false",
                    event.world.getWorldInfo().getWorldName(), event.world.provider.dimensionId);
            gameRule.setOrCreateGameRule("doMobSpawning", "false");
        }

        String ruleName = "doCustomMobSpawning";
        if (!gameRule.hasRule(ruleName)) {
            gameRule.addGameRule(ruleName, "true");
        }
    }
}
