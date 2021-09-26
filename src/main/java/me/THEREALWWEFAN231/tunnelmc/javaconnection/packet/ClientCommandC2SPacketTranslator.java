package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.PlayerActionType;
import com.nukkitx.protocol.bedrock.packet.PlayerActionPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

public class ClientCommandC2SPacketTranslator extends PacketTranslator<ClientCommandC2SPacket> {

	@Override
	public void translate(ClientCommandC2SPacket packet) {

		if (packet.getMode() == Mode.PRESS_SHIFT_KEY) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getId());
			playerActionPacket.setAction(PlayerActionType.START_SNEAK);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		} else if (packet.getMode() == Mode.RELEASE_SHIFT_KEY) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getId());
			playerActionPacket.setAction(PlayerActionType.STOP_SNEAK);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		} else if (packet.getMode() == Mode.START_SPRINTING) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getId());
			playerActionPacket.setAction(PlayerActionType.START_SPRINT);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		} else if (packet.getMode() == Mode.STOP_SPRINTING) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getId());
			playerActionPacket.setAction(PlayerActionType.STOP_SPRINT);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return ClientCommandC2SPacket.class;
	}

}
