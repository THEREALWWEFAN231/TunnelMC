package me.THEREALWWEFAN231.tunnelmc.translator.packet.entity;

import com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;

public class RemoveEntityPacketTranslator extends PacketTranslator<RemoveEntityPacket> {

	@Override
	public void translate(RemoveEntityPacket packet) {

		int id = (int) packet.getUniqueEntityId();

		EntitiesDestroyS2CPacket entitiesDestroyS2CPacket = new EntitiesDestroyS2CPacket(id);

		Client.instance.javaConnection.processServerToClientPacket(entitiesDestroyS2CPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return RemoveEntityPacket.class;
	}

}
