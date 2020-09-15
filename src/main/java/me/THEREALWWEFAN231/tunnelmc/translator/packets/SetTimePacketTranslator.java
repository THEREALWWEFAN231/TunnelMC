package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.SetTimePacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class SetTimePacketTranslator extends PacketTranslator<SetTimePacket> {

	@Override
	public void translate(SetTimePacket packet) {
		WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket = new WorldTimeUpdateS2CPacket(packet.getTime(), packet.getTime(), true);//TODO: remove true and replace it with the gamerule
		Client.instance.javaConnection.processServerToClientPacket(worldTimeUpdateS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return SetTimePacket.class;
	}

}
