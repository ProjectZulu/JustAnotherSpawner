package jas.common.network;

import jas.common.network.packet.PacketManagerServerSync;
import jas.common.network.packet.PacketManagerUpdateCreatureType;
import jas.common.network.packet.PacketManagerUpdateLivingHandler;
import jas.common.network.packet.PacketManagerUpdateSpawnListEntry;

import java.util.EnumSet;
import java.util.HashMap;

public enum PacketID {
    /* PacketID: Unknown Packet, used to send a warning */
    UNKNOWN(-1) {
        @Override
        public PacketManager createPacketManager() {
            return null;
        }
    },
    SERVER_SYNC(0) {
        @Override
        public PacketManager createPacketManager() {
            return new PacketManagerServerSync(iD);
        }
    },
    LIVING_HANDLER(1) {
        @Override
        public PacketManager createPacketManager() {
            return new PacketManagerUpdateLivingHandler(iD);
        }
    },
    CREATURE_TYPE(2) {
        @Override
        public PacketManager createPacketManager() {
            return new PacketManagerUpdateCreatureType(iD);
        }
    },
    SPAWN_ENTRY(3) {
        @Override
        public PacketManager createPacketManager() {
            return new PacketManagerUpdateSpawnListEntry(iD);
        }
    };

    public final int iD;

    public int iD() {
        return iD;
    }

    private static final HashMap<Integer, PacketID> lookupEnum = new HashMap<Integer, PacketID>();
    static {
        for (PacketID packetID : EnumSet.allOf(PacketID.class))
            lookupEnum.put(packetID.iD, packetID);
    }

    PacketID(int iD) {
        this.iD = iD;
    }

    /* Return unknown if State Cannot be Found */
    public static PacketID getPacketIDbyIndex(int index) {
        PacketID value = lookupEnum.get(index);
        if (value != null) {
            return value;
        } else {
            return PacketID.UNKNOWN;
        }
    }

    public abstract PacketManager createPacketManager();
}
