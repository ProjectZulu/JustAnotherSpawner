package jas.common.network;

import java.util.EnumSet;
import java.util.HashMap;

public enum PacketID {
    /* PacketID: Unknown Packet, send a Warning */
    unknown(0) {
        @Override
        public <T extends PacketManager> T createPacketManager() {
            return null;
        }
    };

    public final int index;

    public int index() {
        return index;
    }

    private static final HashMap<Integer, PacketID> lookupEnum = new HashMap<Integer, PacketID>();
    static {
        for (PacketID packetID : EnumSet.allOf(PacketID.class))
            lookupEnum.put(packetID.index, packetID);
    }

    PacketID(int index) {
        this.index = index;
    }

    /* Return unknown if State Cannot be Found */
    public static PacketID getPacketIDbyIndex(int index) {
        PacketID value = lookupEnum.get(index);
        if (value != null) {
            return value;
        } else {
            return unknown;
        }
    }

    public abstract <T extends PacketManager> T createPacketManager();
}
