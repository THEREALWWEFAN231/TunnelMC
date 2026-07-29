package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import org.cloudburstmc.protocol.bedrock.packet.TakeItemEntityPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;

public class TakeItemEntityPacketTranslator extends PacketTranslator<TakeItemEntityPacket> {

	@Override
	public void translate(TakeItemEntityPacket packet) {

		int entityId = (int) packet.getItemRuntimeEntityId();
		int collectorId = (int) packet.getRuntimeEntityId();
		int stackAmount = 1;//this probably actually needs the correct value, ill test that later, we can probably get the value from the item entity in the world

		ClientboundTakeItemEntityPacket itemPickupAnimationS2CPacket = new ClientboundTakeItemEntityPacket(entityId, collectorId, stackAmount);

		Client.instance.javaConnection.processServerToClientPacket(itemPickupAnimationS2CPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return TakeItemEntityPacket.class;
	}

}
