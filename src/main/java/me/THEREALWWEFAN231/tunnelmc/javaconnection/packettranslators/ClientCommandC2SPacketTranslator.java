package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.packet.PlayerActionPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerActionPacket.Action;

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
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());
			playerActionPacket.setAction(Action.START_SNEAK);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		} else if (packet.getMode() == Mode.RELEASE_SHIFT_KEY) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());
			playerActionPacket.setAction(Action.STOP_SNEAK);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		} else if (packet.getMode() == Mode.START_SPRINTING) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());
			playerActionPacket.setAction(Action.START_SPRINT);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		} else if (packet.getMode() == Mode.STOP_SPRINTING) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());
			playerActionPacket.setAction(Action.STOP_SPRINT);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			
			Client.instance.sendPacket(playerActionPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return ClientCommandC2SPacket.class;
	}

}
