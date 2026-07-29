package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

public class RemoveEntityPacketTranslator extends PacketTranslator<RemoveEntityPacket> {

	@Override
	public void translate(RemoveEntityPacket packet) {

		int id = (int) packet.getUniqueEntityId();

		ClientboundRemoveEntitiesPacket entitiesDestroyS2CPacket = new ClientboundRemoveEntitiesPacket(id);

		Client.instance.javaConnection.processServerToClientPacket(entitiesDestroyS2CPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return RemoveEntityPacket.class;
	}

}
