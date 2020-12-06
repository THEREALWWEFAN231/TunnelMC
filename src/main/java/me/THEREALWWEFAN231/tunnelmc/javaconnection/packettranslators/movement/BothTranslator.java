package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Both;

public class BothTranslator extends PacketTranslator<PlayerMoveC2SPacket.Both> {

	@Override
	public void translate(Both packet) {
		PlayerMoveC2SPacketTranslator.translateMovementPacket((PlayerMoveC2SPacket.Both) packet, MovePlayerPacket.Mode.NORMAL);
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.Both.class;
	}

}
