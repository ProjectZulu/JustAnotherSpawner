package jas.common.gui;

import jas.common.gui.elements.GuiSpawner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    @SuppressWarnings("incomplete-switch")
    public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {

        switch (GuiID.getGuiIDByID(guiID)) {
        case Unknown:
            throw new IllegalStateException("GuiID with id %s does not exist" + guiID);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        switch (GuiID.getGuiIDByID(guiID)) {
        case Spawner: 
            return new GuiSpawner();
        case Unknown:
            throw new IllegalStateException("GuiID with id %s does not exist" + guiID);
        }
        return null;
    }
}
