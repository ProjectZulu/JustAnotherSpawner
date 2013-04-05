package jas.common.network.packet;

import jas.common.network.PacketManager;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import cpw.mods.fml.common.network.Player;

//TODO: Each Packet LIke this needs 3 types; Add, Update, Remove. Well, most. Creature Type and LivingHandler cannot be removed. Not added either? Not yet.
public class PacketManagerUpdateLivingHandler extends PacketManager {
    String entityName;
    String creatureTypeID;
    boolean useModLocationCheck;
    boolean shouldSpawn;
    boolean forceDespawn;

    public PacketManagerUpdateLivingHandler(int packetID) {
        super(packetID);
    }

    public void setPacketData(LivingHandler livingHandler) {
        entityName = (String) EntityList.classToStringMapping.get(livingHandler.entityClass);
        creatureTypeID = livingHandler.creatureTypeID;
        useModLocationCheck = livingHandler.useModLocationCheck;
        shouldSpawn = livingHandler.shouldSpawn;
        forceDespawn = livingHandler.forceDespawn;
    }

    @Override
    protected void writePacketData(DataOutputStream dataStream) throws IOException {
        dataStream.writeUTF(entityName);
        dataStream.writeUTF(creatureTypeID);
        dataStream.writeBoolean(useModLocationCheck);
        dataStream.writeBoolean(shouldSpawn);
        dataStream.writeBoolean(forceDespawn);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean processPacket(DataInputStream dataStream, Player player) {
        try {
            entityName = dataStream.readUTF();
            creatureTypeID = dataStream.readUTF();
            useModLocationCheck = dataStream.readBoolean();
            shouldSpawn = dataStream.readBoolean();
            forceDespawn = dataStream.readBoolean();
            CreatureHandlerRegistry.INSTANCE.updateLivingHandler(
                    (Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(entityName), creatureTypeID,
                    useModLocationCheck, shouldSpawn, forceDespawn);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
