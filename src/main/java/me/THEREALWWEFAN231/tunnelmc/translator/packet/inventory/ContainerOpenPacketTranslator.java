package me.THEREALWWEFAN231.tunnelmc.translator.packet.inventory;

import com.nukkitx.nbt.NbtMap;
import com.nukkitx.protocol.bedrock.data.inventory.ContainerType;
import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.container.ContainerTypeTranslator;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.LiteralText;

public class ContainerOpenPacketTranslator extends PacketTranslator<ContainerOpenPacket> {

	@Override
	public void translate(ContainerOpenPacket packet) {

		//for now just return who cares
		if (packet.getType() == ContainerType.INVENTORY) {
			return;
		}
		
		ScreenHandlerType<?> screenHandlerType = ContainerTypeTranslator.bedrockToJava(packet.getType());
		if(screenHandlerType == null) {
			System.out.println("No screen handler " + packet.getType());
			return;
		}
		
		//the nbt map is suppose to have the name(or at least custom name) of the container, but the nbt map is null unless we placed the chest(or what ever) during the current "session", the BlockEntityDataPacket packet isn't sent unless we placed the block, so when we join the game, this returns null, I have no idea how to get the data(I don't know which packet it is sent in when we join a server), proxy pass for what ever reason doesn't show me anything relevant
		NbtMap blockEntityData = Client.instance.blockEntityDataCache.getDataFromBlockPosition(packet.getBlockPosition());

		OpenScreenS2CPacket openScreenS2CPacket = new OpenScreenS2CPacket(packet.getId() & 0xff, screenHandlerType, new LiteralText("Name TODO"));
		Client.instance.javaConnection.processServerToClientPacket(openScreenS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return ContainerOpenPacket.class;
	}

}