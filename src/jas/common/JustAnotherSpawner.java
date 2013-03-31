package jas.common;

import jas.common.proxy.CommonProxy;
import jas.common.spawner.ChunkSpawner;
import jas.common.spawner.SpawnerTicker;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;

import net.minecraft.world.GameRules;
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
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = DefaultProps.MODID, name = DefaultProps.MODNAME, version = DefaultProps.VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
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
        JASLog.configureLogging(Properties.debugMode);
        TickRegistry.registerTickHandler(new SpawnerTicker(), Side.SERVER);
        MinecraftForge.TERRAIN_GEN_BUS.register(new ChunkSpawner());
    }

    @Init
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EntityDespawner());
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {

    }

    @ServerStarting
    public void serverStart(FMLServerStartingEvent event) {
        CreatureTypeRegistry.INSTANCE.initializeFromConfig(modConfigDirectoryFile, event.getServer());
        CreatureHandlerRegistry.INSTANCE.findProcessEntitesForHandlers(modConfigDirectoryFile, event.getServer());

        GameRules gameRule = event.getServer().worldServerForDimension(0).getGameRules();
        String ruleName = "doCustomMobSpawning";
        if (gameRule.hasRule(ruleName)) {
        } else {
            gameRule.addGameRule(ruleName, "true");
        }
    }
}
