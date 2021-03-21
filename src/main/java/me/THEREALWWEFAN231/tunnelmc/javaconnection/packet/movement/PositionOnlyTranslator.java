package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet.movement;

import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionOnly;

public class PositionOnlyTranslator extends PacketTranslator<PlayerMoveC2SPacket.PositionOnly> {

	@Override
	public void translate(PositionOnly packet) {
		PlayerMoveC2SPacketTranslator.translateMovementPacket((PlayerMoveC2SPacket.PositionOnly) packet, MovePlayerPacket.Mode.NORMAL);
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.PositionOnly.class;
	}

}
