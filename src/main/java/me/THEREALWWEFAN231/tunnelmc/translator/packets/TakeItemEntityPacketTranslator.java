package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.TakeItemEntityPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;

public class TakeItemEntityPacketTranslator extends PacketTranslator<TakeItemEntityPacket> {

	@Override
	public void translate(TakeItemEntityPacket packet) {

		int entityId = (int) packet.getItemRuntimeEntityId();
		int collectorId = (int) packet.getRuntimeEntityId();
		int stackAmount = 1;//this probably actually needs the correct value, ill test that later, we can probably get the value from the item entity in the world

		ItemPickupAnimationS2CPacket itemPickupAnimationS2CPacket = new ItemPickupAnimationS2CPacket(entityId, collectorId, stackAmount);

		Client.instance.javaConnection.processServerToClientPacket(itemPickupAnimationS2CPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return TakeItemEntityPacket.class;
	}

}
