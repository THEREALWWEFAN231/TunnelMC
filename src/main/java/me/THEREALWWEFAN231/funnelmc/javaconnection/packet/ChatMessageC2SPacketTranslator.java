package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType;
import org.cloudburstmc.protocol.bedrock.packet.CommandRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ServerboundChatPacket;

public class ChatMessageC2SPacketTranslator extends PacketTranslator<ServerboundChatPacket> {

	@Override
	public void translate(ServerboundChatPacket packet) {
		if (packet.message().startsWith("/")) {
			CommandRequestPacket commandPacket = new CommandRequestPacket();
			commandPacket.setCommand(packet.message());
			commandPacket.setInternal(false); // ???
			commandPacket.setCommandOriginData(new CommandOriginData(CommandOriginType.PLAYER, Client.instance.authData.getIdentity(), "", 0));

			Client.instance.sendPacket(commandPacket);
		} else {
			TextPacket textPacket = new TextPacket();
			textPacket.setType(TextPacket.Type.CHAT);
			textPacket.setNeedsTranslation(false);
			textPacket.setSourceName(Client.instance.authData.getDisplayName());
			textPacket.setMessage(packet.message());
			textPacket.setXuid(Client.instance.authData.getXuid());

			Client.instance.sendPacket(textPacket);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundChatPacket.class;
	}

}
