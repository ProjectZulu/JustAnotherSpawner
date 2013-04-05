package jas.common.proxy;

import jas.common.gui.GuiKeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void registerKeyBinding() {
        KeyBindingRegistry.registerKeyBinding(new GuiKeyBinding());
    }
}
