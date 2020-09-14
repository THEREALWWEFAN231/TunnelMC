package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

public class PlayStatusPacketTranslator extends PacketTranslator<PlayStatusPacket> {

	@Override
	public void translate(PlayStatusPacket packet) {
		if (packet.getStatus() == PlayStatusPacket.Status.PLAYER_SPAWN) {
			RequestChunkRadiusPacket requestChunkRadiusPacket = new RequestChunkRadiusPacket();
			requestChunkRadiusPacket.setRadius(TunnelMC.mc.options.viewDistance);

			Client.instance.sendPacketImmediately(requestChunkRadiusPacket);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayStatusPacket.class;
	}

}
