package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.nukkitx.protocol.bedrock.data.command.CommandOriginData;
import com.nukkitx.protocol.bedrock.data.command.CommandOriginType;
import com.nukkitx.protocol.bedrock.packet.CommandRequestPacket;
import com.nukkitx.protocol.bedrock.packet.TextPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class ChatMessageC2SPacketTranslator extends PacketTranslator<ChatMessageC2SPacket> {

	@Override
	public void translate(ChatMessageC2SPacket packet) {
		if (packet.getChatMessage().startsWith("/")) {
			CommandRequestPacket commandPacket = new CommandRequestPacket();
			commandPacket.setCommand(packet.getChatMessage());
			commandPacket.setInternal(false); // ???
			commandPacket.setCommandOriginData(new CommandOriginData(CommandOriginType.PLAYER, Client.instance.authData.getIdentity(), "", 0));

			Client.instance.sendPacket(commandPacket);
		} else {
			TextPacket textPacket = new TextPacket();
			textPacket.setType(TextPacket.Type.CHAT);
			textPacket.setNeedsTranslation(false);
			textPacket.setSourceName(Client.instance.authData.getDisplayName());
			textPacket.setMessage(packet.getChatMessage());
			textPacket.setXuid(Client.instance.authData.getXuid());

			Client.instance.sendPacket(textPacket);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return ChatMessageC2SPacket.class;
	}

}
