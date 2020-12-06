package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.nukkitx.protocol.bedrock.packet.ContainerClosePacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.mixins.interfaces.IMixinCloseHandledScreenC2SPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class CloseHandledScreenC2SPacketTranslator extends PacketTranslator<CloseHandledScreenC2SPacket> {

	@Override
	public void translate(CloseHandledScreenC2SPacket packet) {

		ContainerClosePacket containerClosePacket = new ContainerClosePacket();
		containerClosePacket.setId((byte) ((IMixinCloseHandledScreenC2SPacket) packet).getSyncId());
		
		Client.instance.sendPacket(containerClosePacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return CloseHandledScreenC2SPacket.class;
	}

}