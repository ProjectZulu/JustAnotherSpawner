package jas.common;

import jas.common.gui.GuiHandler;
import jas.common.network.PacketHandler;
import jas.common.proxy.CommonProxy;
import jas.common.spawner.ChunkSpawner;
import jas.common.spawner.SpawnerTicker;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.structure.BiomeHandlerRegistry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;
import jas.compatability.CompatabilityManager;

import java.io.File;
import java.io.IOException;

import net.minecraft.world.GameRules;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = DefaultProps.MODID, name = DefaultProps.MODNAME, version = DefaultProps.VERSION, dependencies = "after:*")
@NetworkMod(clientSideRequired = false, serverSideRequired = false, channels = { DefaultProps.defaultChannel }, packetHandler = PacketHandler.class)
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

    ImportedSpawnList importedSpawnList;
    BiomeBlacklist biomeBlacklist;
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event) {
        modConfigDirectoryFile = event.getModConfigurationDirectory();
        Properties.loadProperties(modConfigDirectoryFile);
        JASLog.configureLogging(modConfigDirectoryFile);
        MinecraftForge.EVENT_BUS.register(this);
        // proxy.registerKeyBinding();
    }

    @Init
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EntityDespawner());
        NetworkRegistry.instance().registerGuiHandler(modInstance, new GuiHandler());
        CompatabilityManager.addCompatabilityResources();
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {
        BiomeDictionary.registerAllBiomes();
        biomeBlacklist = new BiomeBlacklist(modConfigDirectoryFile);
        MinecraftForge.TERRAIN_GEN_BUS.register(new ChunkSpawner(biomeBlacklist));
        TickRegistry.registerTickHandler(new SpawnerTicker(biomeBlacklist), Side.SERVER);
        importedSpawnList = new ImportedSpawnList(biomeBlacklist, Properties.emptyVanillaSpawnLists);
    }

    @ServerStarting
    public void serverStart(FMLServerStartingEvent event) {
        Properties.loadWorldSaveConfiguration(modConfigDirectoryFile, event.getServer().worldServers[0]);
        importDefaultFiles(modConfigDirectoryFile);
        Properties.loadWorldProperties(modConfigDirectoryFile);
        BiomeGroupRegistry.INSTANCE.createBiomeGroups(modConfigDirectoryFile);
        CreatureTypeRegistry.INSTANCE.initializeFromConfig(modConfigDirectoryFile);
        CreatureHandlerRegistry.INSTANCE.serverStartup(modConfigDirectoryFile, event.getServer().worldServers[0],
                importedSpawnList);
        BiomeHandlerRegistry.INSTANCE.setupHandlers(modConfigDirectoryFile, event.getServer().worldServers[0]);
    }

    @ForgeSubscribe
    public void worldLoad(WorldEvent.Load event) {
        GameRules gameRule = event.world.getGameRules();
        if (gameRule != null && Properties.turnGameruleSpawningOff) {
            JASLog.info("Setting GameRule doMobSpawning for %s-%s to false", event.world.getWorldInfo().getWorldName(),
                    event.world.provider.dimensionId);
            gameRule.setOrCreateGameRule("doMobSpawning", "false");
        }

        String ruleName = "doCustomMobSpawning";
        if (!gameRule.hasRule(ruleName)) {
            gameRule.addGameRule(ruleName, "true");
        }
    }
    
    private void importDefaultFiles(File modConfigDirectoryFile) {
        if (Properties.importName.trim().equals("")) {
            return;
        }
        File worldFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR + Properties.saveName);
        File importFolder = new File(modConfigDirectoryFile, DefaultProps.WORLDSETTINGSDIR + Properties.importName);
        if (worldFolder.exists() || !importFolder.exists()) {
            return;
        }
        try {
            FileUtilities.copy(importFolder, worldFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
