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
//TODO: add OptionalParameter
public class PacketManagerUpdateLivingHandler extends PacketManager {
    String entityName;
    String creatureTypeID;
    boolean shouldSpawn;

    public PacketManagerUpdateLivingHandler(int packetID) {
        super(packetID);
    }

    public void setPacketData(LivingHandler livingHandler) {
        entityName = (String) EntityList.classToStringMapping.get(livingHandler.entityClass);
        creatureTypeID = livingHandler.creatureTypeID;
        shouldSpawn = livingHandler.shouldSpawn;
    }

    @Override
    protected void writePacketData(DataOutputStream dataStream) throws IOException {
        dataStream.writeUTF(entityName);
        dataStream.writeUTF(creatureTypeID);
        dataStream.writeBoolean(shouldSpawn);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean processPacket(DataInputStream dataStream, Player player) {
        try {
            entityName = dataStream.readUTF();
            creatureTypeID = dataStream.readUTF();
            shouldSpawn = dataStream.readBoolean();
            CreatureHandlerRegistry.INSTANCE.updateLivingHandler(
                    (Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(entityName), creatureTypeID,
                    shouldSpawn);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
