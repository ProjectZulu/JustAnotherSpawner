package jas.common;

import jas.common.proxy.CommonProxy;

import java.io.File;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = DefaultProps.MODID, name = DefaultProps.MODNAME, version = DefaultProps.VERSION)
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class JustAnotherSpawner {

    @Instance(DefaultProps.MODID)
    public static JustAnotherSpawner modInstance;

    @SidedProxy(clientSide = "jas.common.proxy.ClientProxy", serverSide = "jas.common.proxy.CommonProxy")
    public static CommonProxy proxy;

    private File modConfigDirectoryFile;

    @PreInit
    public void preInit(FMLPreInitializationEvent event) {
        JASLog.configureLogging();
        modConfigDirectoryFile = event.getModConfigurationDirectory();
        TickRegistry.registerTickHandler(new SpawnTicker(), Side.SERVER);
    }

    @Init
    public void load(FMLInitializationEvent event) {
//        CreatureHandlerRegistry.INSTANCE.findValidEntities();
//        CreatureHandlerRegistry.INSTANCE.generateHandlers(modConfigDirectoryFile);
//        CreatureHandlerRegistry.INSTANCE.generateSpawnLists(modConfigDirectoryFile);
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {

    }
}
