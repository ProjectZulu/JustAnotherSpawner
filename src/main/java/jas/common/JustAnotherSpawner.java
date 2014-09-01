package jas.common;

import jas.api.CompatibilityRegistrationEvent;
import jas.common.command.CommandJAS;
import jas.common.proxy.CommonProxy;
import jas.common.spawner.ChunkSpawner;
import jas.common.spawner.SpawnerTicker;

import java.io.File;

import net.minecraft.world.GameRules;
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

    private static GlobalSettings globalSettings;

    public static GlobalSettings globalSettings() {
        return globalSettings;
    }

    private static WorldSettings worldSettings;

    public static WorldSettings worldSettings() {
        return worldSettings;
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

        MinecraftForge.EVENT_BUS.register(this);
        
//    	TagsObject tags = new TagsObject();
//		Serializable expression = MVEL.compileExpression("sky()==false");
//		Boolean restult = (Boolean) MVEL.executeExpression(expression, tags);
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
        event.registerServerCommand(new CommandJAS());
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
