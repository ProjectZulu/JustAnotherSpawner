package jas.common.gui;

import jas.common.JustAnotherSpawner;
import jas.common.network.PacketID;
import jas.common.network.packet.PacketManagerServerSync;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiKeyBinding extends KeyHandler {

    public GuiKeyBinding() {
        this(new KeyBinding[] { new KeyBinding("Open JAS Gui", Keyboard.KEY_J) }, new boolean[] { false });
    }

    public GuiKeyBinding(KeyBinding[] keyBindings, boolean[] repeats) {
        super(keyBindings, repeats);
    }

    @Override
    public String getLabel() {
        return "JASGui";
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (tickEnd && Minecraft.getMinecraft().currentScreen == null && player != null) {
            CreatureHandlerRegistry.INSTANCE.clientStartup(Minecraft.getMinecraft().theWorld);

            PacketManagerServerSync packetManager = (PacketManagerServerSync) PacketID.SERVER_SYNC
                    .createPacketManager();
            packetManager.setPacketData(true);
            PacketDispatcher.sendPacketToServer(packetManager.createPacket());

            player.openGui(JustAnotherSpawner.modInstance, GuiID.Spawner.iD, Minecraft.getMinecraft().theWorld,
                    (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }
}
