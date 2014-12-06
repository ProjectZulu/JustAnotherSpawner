package jas.gui.proxy;

import jas.gui.DisplayUnitRegistry;
import jas.gui.JASGUI;
import jas.gui.Properties;
import jas.gui.display.DisplayTicker;
import jas.gui.display.GuiHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerDisplayTicker(DisplayUnitRegistry displayRegistry) {
        DisplayTicker ticker = new DisplayTicker(displayRegistry);
        MinecraftForge.EVENT_BUS.register(ticker);
        FMLCommonHandler.instance().bus().register(ticker);
    }

    @Override
    public void registerGuiHandling(DisplayUnitRegistry displayRegistry, Properties properties) {
        GuiHandler guiHandler = new GuiHandler(displayRegistry, properties);
        NetworkRegistry.INSTANCE.registerGuiHandler(JASGUI.modInstance, guiHandler);
        FMLCommonHandler.instance().bus().register(guiHandler);
    }
}
