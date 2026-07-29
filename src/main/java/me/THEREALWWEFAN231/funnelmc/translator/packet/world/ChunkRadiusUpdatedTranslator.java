package me.THEREALWWEFAN231.funnelmc.translator.packet.world;

import org.cloudburstmc.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;

public class ChunkRadiusUpdatedTranslator extends PacketTranslator<ChunkRadiusUpdatedPacket> {

	@Override
	public void translate(ChunkRadiusUpdatedPacket packet) {
		Client.instance.javaConnection.processServerToClientPacket(new ClientboundSetChunkCacheRadiusPacket(packet.getRadius()));
	}

	@Override
	public Class<?> getPacketClass() {
		return ChunkRadiusUpdatedPacket.class;
	}

}
