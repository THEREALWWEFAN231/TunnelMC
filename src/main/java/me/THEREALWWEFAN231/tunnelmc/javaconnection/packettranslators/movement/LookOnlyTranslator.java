package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookOnly;

public class LookOnlyTranslator extends PacketTranslator<PlayerMoveC2SPacket.LookOnly> {

	@Override
	public void translate(LookOnly packet) {
		PlayerMoveC2SPacketTranslator.translateMovementPacket((PlayerMoveC2SPacket.LookOnly) packet, MovePlayerPacket.Mode.HEAD_ROTATION);

	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.LookOnly.class;
	}

}
