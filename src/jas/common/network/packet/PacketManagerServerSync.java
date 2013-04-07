package jas.common.network.packet;

import jas.common.network.PacketID;
import jas.common.network.PacketManager;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import net.minecraft.entity.EntityLiving;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Packet is Sent from Client to Server Instructing it To Sync its Spawn data to the Client Marks Server as Locked For
 * Editing
 */
// TODO: Implement lock server
// TODO: use boolean to mark start/end as opposed to StartSeverSync, StopServerSync
public class PacketManagerServerSync extends PacketManager {
    boolean startSync;

    public PacketManagerServerSync(int packetID) {
        super(packetID);
    }

    public void setPacketData(boolean startSync) {
        this.startSync = startSync;
    }

    @Override
    protected void writePacketData(DataOutputStream dataStream) throws IOException {
        dataStream.writeBoolean(startSync);
    }

    @Override
    public boolean processPacket(DataInputStream dataStream, Player player) {
        try {
            setPacketData(dataStream.readBoolean());
            // TODO: Lock/Unlock Server

            /* Sync CreatureType To Client */
            Iterator<CreatureType> typeIterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
            while (typeIterator.hasNext()) {
                CreatureType creatureType = typeIterator.next();
                PacketManagerUpdateCreatureType creaturePacketManager = (PacketManagerUpdateCreatureType) PacketID.CREATURE_TYPE
                        .createPacketManager();
                creaturePacketManager.setPacketData(creatureType);
                PacketDispatcher.sendPacketToPlayer(creaturePacketManager.createPacket(), player);

                /* Sync SpawnListEntries To Client */
                Iterator<SpawnListEntry> spawnIterator = creatureType.getAllSpawns().iterator();
                while (spawnIterator.hasNext()) {
                    SpawnListEntry entry = spawnIterator.next();
                    PacketManagerUpdateSpawnListEntry spawnPacketManager = (PacketManagerUpdateSpawnListEntry) PacketID.SPAWN_ENTRY
                            .createPacketManager();
                    spawnPacketManager.setPacketData(creatureType.typeID, entry);
                    PacketDispatcher.sendPacketToPlayer(spawnPacketManager.createPacket(), player);
                }
            }

            /* Sync LivingHandler To Client */
            Iterator<Class<? extends EntityLiving>> handlerIterator = CreatureHandlerRegistry.INSTANCE.getLivingKeys();
            while (handlerIterator.hasNext()) {
                Class<? extends EntityLiving> livingClass = handlerIterator.next();
                PacketManagerUpdateLivingHandler packetManager = (PacketManagerUpdateLivingHandler) PacketID.LIVING_HANDLER
                        .createPacketManager();
                packetManager.setPacketData(CreatureHandlerRegistry.INSTANCE.getLivingHandler(livingClass));
                PacketDispatcher.sendPacketToPlayer(packetManager.createPacket(), player);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
