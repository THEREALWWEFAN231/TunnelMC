package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.ContainerOpenPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.LiteralText;

public class ContainerOpenPacketTranslator extends PacketTranslator<ContainerOpenPacket> {

	@Override
	public void translate(ContainerOpenPacket packet) {
		
		OpenScreenS2CPacket openScreenS2CPacket = new OpenScreenS2CPacket(packet.getId() & 0xff, ScreenHandlerType.GENERIC_9X3, new LiteralText("idfk"));
		Client.instance.javaConnection.processServerToClientPacket(openScreenS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return ContainerOpenPacket.class;
	}

}