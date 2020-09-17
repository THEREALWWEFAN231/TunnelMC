package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PlayerMoveC2SPacketTranslator extends PacketTranslator<PlayerMoveC2SPacket> {

	@Override
	public void translate(PlayerMoveC2SPacket packet) {
		PlayerMoveC2SPacket playerMoveC2SPacket = (PlayerMoveC2SPacket) packet;
		
		int runtimeId = TunnelMC.mc.player.getEntityId();
		
		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		movePlayerPacket.setRuntimeEntityId(runtimeId);
		movePlayerPacket.setPosition(Vector3f.from(playerMoveC2SPacket.getX(TunnelMC.mc.player.getPos().x), playerMoveC2SPacket.getY(TunnelMC.mc.player.getPos().y) + TunnelMC.mc.player.getStandingEyeHeight(), playerMoveC2SPacket.getZ(TunnelMC.mc.player.getPos().z)));
		movePlayerPacket.setRotation(Vector3f.from(playerMoveC2SPacket.getYaw(TunnelMC.mc.player.yaw), playerMoveC2SPacket.getYaw(TunnelMC.mc.player.pitch), 0));
		movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
		movePlayerPacket.setOnGround(playerMoveC2SPacket.isOnGround());
		Client.instance.sendPacket(movePlayerPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.class;
	}

}
