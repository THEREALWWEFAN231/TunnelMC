package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.nukkitx.protocol.bedrock.packet.TextPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class ChatMessageC2SPacketTranslator extends PacketTranslator<ChatMessageC2SPacket> {

	@Override
	public void translate(ChatMessageC2SPacket packet) {

		TextPacket textPacket = new TextPacket();
		textPacket.setType(TextPacket.Type.CHAT);
		textPacket.setNeedsTranslation(false);
		textPacket.setSourceName(Client.instance.authData.getDisplayName());
		textPacket.setMessage(packet.getChatMessage());
		textPacket.setXuid(Client.instance.authData.getXuid());

		Client.instance.sendPacket(textPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return ChatMessageC2SPacket.class;
	}

}
