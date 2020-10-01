package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Both;

public class BothTranslator extends PacketTranslator<PlayerMoveC2SPacket.Both> {

	@Override
	public void translate(Both packet) {
		Both both = (Both) packet;

		double currentPosX = both.getX(TunnelMC.mc.player.getPos().x);
		double currentPosY = both.getY(TunnelMC.mc.player.getPos().y) + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING);
		double currentPosZ = both.getZ(TunnelMC.mc.player.getPos().z);
		float currentYaw = both.getYaw(TunnelMC.mc.player.yaw);
		float currentPitch = both.getPitch(TunnelMC.mc.player.pitch);
		boolean currentlyOnGround = both.isOnGround();

		if (PlayerMoveC2SPacketTranslator.lastPosX == currentPosX && PlayerMoveC2SPacketTranslator.lastPosY == currentPosY && PlayerMoveC2SPacketTranslator.lastPosZ == currentPosZ && PlayerMoveC2SPacketTranslator.lastYaw == currentYaw && PlayerMoveC2SPacketTranslator.lastPitch == currentPitch && PlayerMoveC2SPacketTranslator.lastOnGround == currentlyOnGround) {
			return;
		}

		int runtimeId = TunnelMC.mc.player.getEntityId();

		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		movePlayerPacket.setRuntimeEntityId(runtimeId);
		movePlayerPacket.setPosition(Vector3f.from(currentPosX, currentPosY, currentPosZ));
		movePlayerPacket.setRotation(Vector3f.from(currentPitch, currentYaw, 0));
		movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
		movePlayerPacket.setOnGround(currentlyOnGround);
		Client.instance.sendPacket(movePlayerPacket);
		
		PlayerMoveC2SPacketTranslator.lastPosX = currentPosX;
		PlayerMoveC2SPacketTranslator.lastPosY = currentPosY;
		PlayerMoveC2SPacketTranslator.lastPosZ = currentPosZ;
		PlayerMoveC2SPacketTranslator.lastYaw = currentYaw;
		PlayerMoveC2SPacketTranslator.lastPitch = currentPitch;
		PlayerMoveC2SPacketTranslator.lastOnGround = currentlyOnGround;

	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.Both.class;
	}

}
