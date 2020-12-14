package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;

import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

public class NetworkChunkPublisherUpdatePacketTranslator extends PacketTranslator<NetworkChunkPublisherUpdatePacket> {

    @Override
    public void translate(NetworkChunkPublisherUpdatePacket packet) {
    	//this "aint it"? It makes chunk loading worse?
        //ChunkRenderDistanceCenterS2CPacket renderDistanceCenterPacket = new ChunkRenderDistanceCenterS2CPacket(packet.getPosition().getX() << 4, packet.getPosition().getZ() << 4);
        //Client.instance.javaConnection.processServerToClientPacket(renderDistanceCenterPacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return NetworkChunkPublisherUpdatePacket.class;
    }
}
