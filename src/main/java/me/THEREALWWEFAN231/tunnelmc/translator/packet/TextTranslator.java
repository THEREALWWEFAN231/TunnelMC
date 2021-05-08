package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.protocol.bedrock.packet.TextPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class TextTranslator extends PacketTranslator<TextPacket> {

	@Override
	public void translate(TextPacket packet) {
		switch (packet.getType()) {
			default: {
				System.out.println("Falling back to raw translation for " + packet.toString());
			}
			case RAW: {
				GameMessageS2CPacket gameMessageS2CPacket = new GameMessageS2CPacket(new LiteralText(packet.getMessage()),
						MessageType.CHAT, new UUID(0, 0));

				Client.instance.javaConnection.processServerToClientPacket(gameMessageS2CPacket);
				break;
			}
			case CHAT: {
				String formattedChatMessage = "<" + packet.getSourceName() + "> " + packet.getMessage();
				GameMessageS2CPacket gameMessageS2CPacket = new GameMessageS2CPacket(new LiteralText(formattedChatMessage),
						MessageType.CHAT, null);

				Client.instance.javaConnection.processServerToClientPacket(gameMessageS2CPacket);
				break;
			}
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return TextPacket.class;
	}
}
