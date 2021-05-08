package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;

public class ChunkRadiusUpdatedTranslator extends PacketTranslator<ChunkRadiusUpdatedPacket> {

	@Override
	public void translate(ChunkRadiusUpdatedPacket packet) {
		Client.instance.javaConnection.processServerToClientPacket(new ChunkLoadDistanceS2CPacket(packet.getRadius()));
	}

	@Override
	public Class<?> getPacketClass() {
		return ChunkRadiusUpdatedPacket.class;
	}

}
