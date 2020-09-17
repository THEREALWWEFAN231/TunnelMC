package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionOnly;

public class PositionOnlyTranslator extends PacketTranslator<PlayerMoveC2SPacket.PositionOnly> {

	@Override
	public void translate(PositionOnly packet) {
		PositionOnly positionOnly = (PositionOnly) packet;

		int runtimeId = TunnelMC.mc.player.getEntityId();

		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		movePlayerPacket.setRuntimeEntityId(runtimeId);
		movePlayerPacket.setPosition(Vector3f.from(positionOnly.getX(TunnelMC.mc.player.getPos().x), positionOnly.getY(TunnelMC.mc.player.getPos().y) + TunnelMC.mc.player.getStandingEyeHeight(), positionOnly.getZ(TunnelMC.mc.player.getPos().z)));
		movePlayerPacket.setRotation(Vector3f.from(positionOnly.getPitch(TunnelMC.mc.player.pitch), positionOnly.getYaw(TunnelMC.mc.player.yaw), 0));
		movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
		movePlayerPacket.setOnGround(positionOnly.isOnGround());
		Client.instance.sendPacket(movePlayerPacket);
		
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.PositionOnly.class;
	}

}
