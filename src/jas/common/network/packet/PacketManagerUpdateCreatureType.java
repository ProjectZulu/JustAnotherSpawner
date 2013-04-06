package jas.common.network.packet;

import jas.common.network.PacketManager;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.Player;

public class PacketManagerUpdateCreatureType extends PacketManager {
    private String typeID;
    private int spawnRate;
    private int maxNumberOfCreature;
    private boolean chunkSpawning;

    public PacketManagerUpdateCreatureType(int packetID) {
        super(packetID);
    }

    public void setPacketData(CreatureType creatureType) {
        setPacketData(creatureType.typeID, creatureType.spawnRate, creatureType.maxNumberOfCreature,
                creatureType.chunkSpawning);
    }

    public void setPacketData(String typeID, int spawnRate, int maxNumberOfCreature, boolean chunkSpawning) {
        this.typeID = typeID;
        this.spawnRate = spawnRate;
        this.maxNumberOfCreature = maxNumberOfCreature;
        this.chunkSpawning = chunkSpawning;
    }

    @Override
    protected void writePacketData(DataOutputStream dataStream) throws IOException {
        dataStream.writeUTF(typeID);
        dataStream.writeInt(spawnRate);
        dataStream.writeInt(maxNumberOfCreature);
        dataStream.writeBoolean(chunkSpawning);
    }

    @Override
    public boolean processPacket(DataInputStream dataStream, Player player) {
        try {
            setPacketData(dataStream.readUTF(), dataStream.readInt(), dataStream.readInt(), dataStream.readBoolean());
            CreatureTypeRegistry.INSTANCE.updateCreatureType(typeID, spawnRate, maxNumberOfCreature, chunkSpawning);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
