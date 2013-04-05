package jas.common.network;

import jas.common.DefaultProps;
import jas.common.JASLog;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        if (packet.channel.equals(DefaultProps.defaultChannel)) {
            JASLog.info("Received Pack");
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
            PacketID packetID;
            try {
                packetID = PacketID.getPacketIDbyIndex(data.readInt());
                JASLog.info("PacketID %s", packetID);
                PacketManager packetManager = packetID.createPacketManager();
                if (!packetManager.processPacket(data, player)) {
                    // TODO: Add Return Type Error for Specific Troubleshooting
                    JASLog.warning("Failed to Process Packet %s", packetManager.getClass().getSimpleName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
