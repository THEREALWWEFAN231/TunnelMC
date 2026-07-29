package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.protocol.bedrock.packet.PlayStatusPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestChunkRadiusPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;

public class PlayStatusPacketTranslator extends PacketTranslator<PlayStatusPacket> {

	@Override
	public void translate(PlayStatusPacket packet) {
		if (packet.getStatus() == PlayStatusPacket.Status.PLAYER_SPAWN) {
//			RequestChunkRadiusPacket requestChunkRadiusPacket = new RequestChunkRadiusPacket();
//			requestChunkRadiusPacket.setRadius(FunnelMC.mc.options.viewDistance);
//
//			Client.instance.sendPacketImmediately(requestChunkRadiusPacket); hmm
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayStatusPacket.class;
	}

}
