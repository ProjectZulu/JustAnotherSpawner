package jas.common.network.packet;

import jas.common.network.PacketManager;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import cpw.mods.fml.common.network.Player;

public class PacketManagerUpdateSpawnListEntry extends PacketManager {
    private String mobName;
    private String creatureType;
    private String biomeName;
    private int spawnWeight;
    private int packSize;
    private int minChunkPack;
    private int maxChunkPack;

    public PacketManagerUpdateSpawnListEntry(int packetID) {
        super(packetID);
    }

    public void setPacketData(String creatureType, SpawnListEntry spawnListEntry) {
        setPacketData(spawnListEntry.livingClass, creatureType, spawnListEntry.pckgName, spawnListEntry.itemWeight,
                spawnListEntry.packSize, spawnListEntry.minChunkPack, spawnListEntry.maxChunkPack);
    }

    public void setPacketData(Class<? extends EntityLiving> livingClass, String creatureType, String biomeName,
            int weight, int packSize, int minChunkPack, int maxChunkPack) {
        this.mobName = (String) EntityList.classToStringMapping.get(livingClass);
        this.creatureType = creatureType;
        this.biomeName = biomeName;
        this.spawnWeight = weight;
        this.packSize = packSize;
        this.minChunkPack = minChunkPack;
        this.maxChunkPack = maxChunkPack;
    }

    @Override
    protected void writePacketData(DataOutputStream dataStream) throws IOException {
        dataStream.writeUTF(mobName);
        dataStream.writeUTF(creatureType);
        dataStream.writeUTF(biomeName);
        dataStream.writeInt(spawnWeight);
        dataStream.writeInt(packSize);
        dataStream.writeInt(minChunkPack);
        dataStream.writeInt(maxChunkPack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean processPacket(DataInputStream dataStream, Player player) {
        try {
            setPacketData((Class<? extends EntityLiving>) EntityList.stringToClassMapping.get(dataStream.readUTF()),
                    dataStream.readUTF(), dataStream.readUTF(), dataStream.readInt(), dataStream.readInt(),
                    dataStream.readInt(), dataStream.readInt());
            CreatureType type = CreatureTypeRegistry.INSTANCE.getCreatureType(creatureType);
            type.updateOrAddSpawn(new SpawnListEntry((Class<? extends EntityLiving>) EntityList.stringToClassMapping
                    .get(mobName), biomeName, spawnWeight, packSize, minChunkPack, maxChunkPack));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
