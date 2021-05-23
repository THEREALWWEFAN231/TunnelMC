package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;

public class NetworkChunkPublisherUpdateTranslator extends PacketTranslator<NetworkChunkPublisherUpdatePacket> {

    @Override
    public void translate(NetworkChunkPublisherUpdatePacket packet) {
        ChunkRenderDistanceCenterS2CPacket renderDistanceCenterPacket = new ChunkRenderDistanceCenterS2CPacket(
                packet.getPosition().getX() >> 4, packet.getPosition().getZ() >> 4);
        Client.instance.javaConnection.processServerToClientPacket(renderDistanceCenterPacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return NetworkChunkPublisherUpdatePacket.class;
    }

}