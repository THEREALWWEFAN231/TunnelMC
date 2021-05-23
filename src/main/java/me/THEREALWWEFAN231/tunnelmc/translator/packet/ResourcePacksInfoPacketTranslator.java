package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.protocol.bedrock.packet.ClientCacheStatusPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePacksInfoPacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

public class ResourcePacksInfoPacketTranslator extends PacketTranslator<ResourcePacksInfoPacket> {

	@Override
	public void translate(ResourcePacksInfoPacket packet) {
		Client.instance.sendPacketImmediately(new ClientCacheStatusPacket());

		ResourcePackClientResponsePacket resourcePackClientResponsePacket = new ResourcePackClientResponsePacket();
		resourcePackClientResponsePacket.setStatus(ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS);

		Client.instance.sendPacketImmediately(resourcePackClientResponsePacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return ResourcePacksInfoPacket.class;
	}

}