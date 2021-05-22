package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.RespawnPacket;
import com.nukkitx.protocol.bedrock.packet.RespawnPacket.State;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;

public class ClientStatusC2SPacketTranslator extends PacketTranslator<ClientStatusC2SPacket> {

	@Override
	public void translate(ClientStatusC2SPacket packet) {
		
		switch (packet.getMode()) {
		case PERFORM_RESPAWN:
			
			//when the player clicks respawn send the client_ready respawn packet
			RespawnPacket respawnPacket = new RespawnPacket();
			respawnPacket.setPosition(Vector3f.ZERO);
			respawnPacket.setState(State.CLIENT_READY);
			respawnPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());
			
			Client.instance.sendPacket(respawnPacket);
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public Class<?> getPacketClass() {
		return ClientStatusC2SPacket.class;
	}

}
