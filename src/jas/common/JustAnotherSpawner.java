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

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
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

    private File modConfigDirectoryFile;

    @PreInit
    public void preInit(FMLPreInitializationEvent event) {
        modConfigDirectoryFile = event.getModConfigurationDirectory();
        Properties.loadProperties(modConfigDirectoryFile);
        JASLog.configureLogging(modConfigDirectoryFile);
        TickRegistry.registerTickHandler(new SpawnerTicker(), Side.SERVER);
        MinecraftForge.TERRAIN_GEN_BUS.register(new ChunkSpawner());
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
        if (Properties.emptyVanillaSpawnLists) {
            clearVanillaSpawnLists();
        }
    }

    @ServerStarting
    public void serverStart(FMLServerStartingEvent event) {
        Properties.loadWorldSaveConfiguration(modConfigDirectoryFile, event.getServer());
        importDefaultFiles(modConfigDirectoryFile);
        Properties.loadWorldProperties(modConfigDirectoryFile, event.getServer());
        BiomeGroupRegistry.INSTANCE.createBiomeGroups(modConfigDirectoryFile, event.getServer());
        CreatureTypeRegistry.INSTANCE.initializeFromConfig(modConfigDirectoryFile, event.getServer());
        CreatureHandlerRegistry.INSTANCE.serverStartup(modConfigDirectoryFile, event.getServer().worldServers[0]);
        BiomeHandlerRegistry.INSTANCE.setupHandlers(modConfigDirectoryFile, event.getServer().worldServers[0]);

        if (Properties.emptyVanillaSpawnLists) {
            clearVanillaSpawnLists();
        }

        GameRules gameRule = event.getServer().worldServerForDimension(0).getGameRules();
        if (Properties.turnGameruleSpawningOff) {
            JASLog.info("Setting GameRule doMobSpawning to false");
            gameRule.setOrCreateGameRule("doMobSpawning", "false");
        }

        String ruleName = "doCustomMobSpawning";
        if (gameRule.hasRule(ruleName)) {
        } else {
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

    private void clearVanillaSpawnLists() {
        JASLog.info("Emptying Vanilla Spawn Lists.");
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome == null) {
                continue;
            }
            for (EnumCreatureType type : EnumCreatureType.values()) {
                if (biome.getSpawnableList(type) != null) {
                    biome.getSpawnableList(type).clear();
                }
            }
        }
    }
}
