package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.protocol.bedrock.packet.AnimatePacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

public class HandSwingC2SPacketTranslator extends PacketTranslator<HandSwingC2SPacket> {

	@Override
	public void translate(HandSwingC2SPacket packet) {
		AnimatePacket animatePacket = new AnimatePacket();
		animatePacket.setAction(AnimatePacket.Action.SWING_ARM);
		animatePacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());
		Client.instance.sendPacket(animatePacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return HandSwingC2SPacket.class;
	}

}
