package me.THEREALWWEFAN231.funnelmc.javaconnection.packet.movement;

import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class BothTranslator extends PacketTranslator<ServerboundMovePlayerPacket.PosRot> {

	@Override
	public void translate(ServerboundMovePlayerPacket.PosRot packet) {
		PlayerMoveTranslator.translateMovementPacket(packet, MovePlayerPacket.Mode.NORMAL);
	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundMovePlayerPacket.PosRot.class;
	}

}
