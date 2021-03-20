package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.TextPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.LiteralText;

public class TextTranslator extends PacketTranslator<TextPacket> {

	@Override
	public void translate(TextPacket packet) {

		if (packet.getType() == TextPacket.Type.RAW) {
			
			GameMessageS2CPacket gameMessageS2CPacket = new GameMessageS2CPacket(new LiteralText(packet.getMessage()), MessageType.CHAT, null);//so we put this to null, i dont see how that would be a problem, but if it is, use the uuid 00000000-0000-0000-0000-000000000000

			Client.instance.javaConnection.processServerToClientPacket(gameMessageS2CPacket);
		} else if (packet.getType() == TextPacket.Type.CHAT) {

			String formattedChatMessage = "<" + packet.getSourceName() + "> " + packet.getMessage(); 
			
			GameMessageS2CPacket gameMessageS2CPacket = new GameMessageS2CPacket(new LiteralText(formattedChatMessage), MessageType.CHAT, null);//so we put this to null, i dont see how that would be a problem, but if it is, use the uuid 00000000-0000-0000-0000-000000000000

			Client.instance.javaConnection.processServerToClientPacket(gameMessageS2CPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return TextPacket.class;
	}

}
