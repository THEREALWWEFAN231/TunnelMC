package me.THEREALWWEFAN231.tunnelmc.translator.packet.inventory;

import com.nukkitx.nbt.NbtMap;
import com.nukkitx.protocol.bedrock.data.inventory.ContainerType;
import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.container.type.ContainerTypeTranslator;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.LiteralText;

public class ContainerOpenPacketTranslator extends PacketTranslator<ContainerOpenPacket> {

	@Override
	public void translate(ContainerOpenPacket packet) {
		if (packet.getType() == ContainerType.INVENTORY) {
			return;
		}
		
		Client.instance.openContainerId = packet.getId();
		
		ScreenHandlerType<?> screenHandlerType = ContainerTypeTranslator.bedrockToJava(packet.getType());
		if(screenHandlerType == null) {
			System.out.println("No screen handler " + packet.getType());
			return;
		}

		NbtMap blockEntityData = Client.instance.blockEntityDataCache.getDataFromBlockPosition(packet.getBlockPosition());

		/*
		 * TODO: This is going to be empty sometimes because the block entity data isn't being updated all the time.
		 *  Decode the block entity data, save it in the cache and then reference it here.
 		 */
		String name = blockEntityData.getString("id");
		if (blockEntityData.getString("CustomName") != null) {
			name = blockEntityData.getString("CustomName");
		}

		OpenScreenS2CPacket openScreenS2CPacket = new OpenScreenS2CPacket(packet.getId() & 0xff, screenHandlerType, new LiteralText(name));
		Client.instance.javaConnection.processServerToClientPacket(openScreenS2CPacket);

		Client.instance.containers.setCurrentlyOpenContainer(new BedrockContainer(27, packet.getId()));
	}

	@Override
	public Class<?> getPacketClass() {
		return ContainerOpenPacket.class;
	}

}