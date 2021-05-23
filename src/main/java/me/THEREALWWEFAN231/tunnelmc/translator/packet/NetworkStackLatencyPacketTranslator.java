package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.protocol.bedrock.packet.NetworkStackLatencyPacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

public class NetworkStackLatencyPacketTranslator extends PacketTranslator<NetworkStackLatencyPacket> {

    @Override
    public void translate(NetworkStackLatencyPacket packet) {
        if (packet.isFromServer()) {
            NetworkStackLatencyPacket resp = new NetworkStackLatencyPacket();
            resp.setTimestamp(System.currentTimeMillis());
            Client.instance.sendPacketImmediately(resp);
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return NetworkStackLatencyPacket.class;
    }

}