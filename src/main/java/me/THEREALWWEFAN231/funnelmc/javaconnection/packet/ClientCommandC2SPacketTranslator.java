package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class ClientCommandC2SPacketTranslator extends PacketTranslator<ServerboundPlayerCommandPacket> {

	// Note: sneak (shift) state moved to ServerboundPlayerInputPacket in modern MC -
	// see PlayerInputC2SPacketTranslator.
	@Override
	public void translate(ServerboundPlayerCommandPacket packet) {

		if (packet.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());
			playerActionPacket.setAction(PlayerActionType.START_SPRINT);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			playerActionPacket.setResultPosition(Vector3i.ZERO);

			Client.instance.sendPacket(playerActionPacket);
		} else if (packet.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());
			playerActionPacket.setAction(PlayerActionType.STOP_SPRINT);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			playerActionPacket.setResultPosition(Vector3i.ZERO);

			Client.instance.sendPacket(playerActionPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundPlayerCommandPacket.class;
	}

}
