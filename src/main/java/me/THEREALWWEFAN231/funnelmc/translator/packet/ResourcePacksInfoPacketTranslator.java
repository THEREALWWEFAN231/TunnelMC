package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.protocol.bedrock.packet.ClientCacheStatusPacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePacksInfoPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;

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
