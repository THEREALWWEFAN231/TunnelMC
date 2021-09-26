package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet.movement;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PlayerMoveTranslator extends PacketTranslator<PlayerMoveC2SPacket> {

	//so java edition sends movement packets every x(i forgot) ticks  even if we didn't move, bedrock doesn't do this, so we basically try to ignore these packets
	public static double lastPosX;
	public static double lastPosY = -Double.MAX_VALUE;//just incase we for some reason spawn at 0 0 0, with 0 ayw and 0 pitch :flushed: even though that shouldn't be a problem
	public static double lastPosZ;
	public static float lastYaw;
	public static float lastPitch;
	public static boolean lastOnGround;

	@Override
	public void translate(PlayerMoveC2SPacket packet) {
		//this shouldn't even be called? I don't know, doesn't matter
		PlayerMoveTranslator.translateMovementPacket(packet, MovePlayerPacket.Mode.NORMAL);
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.class;
	}

	public static void translateMovementPacket(PlayerMoveC2SPacket playerMoveC2SPacket, MovePlayerPacket.Mode mode) {
		double currentPosX = playerMoveC2SPacket.getX(TunnelMC.mc.player.getPos().x);
		double currentPosY = playerMoveC2SPacket.getY(TunnelMC.mc.player.getPos().y) + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING);
		double currentPosZ = playerMoveC2SPacket.getZ(TunnelMC.mc.player.getPos().z);
		float currentYaw = playerMoveC2SPacket.getYaw(TunnelMC.mc.player.getYaw());
		float currentPitch = playerMoveC2SPacket.getPitch(TunnelMC.mc.player.getPitch());
		boolean currentlyOnGround = playerMoveC2SPacket.isOnGround();

		if (PlayerMoveTranslator.lastPosX == currentPosX && PlayerMoveTranslator.lastPosY == currentPosY && PlayerMoveTranslator.lastPosZ == currentPosZ && PlayerMoveTranslator.lastYaw == currentYaw && PlayerMoveTranslator.lastPitch == currentPitch && PlayerMoveTranslator.lastOnGround == currentlyOnGround) {
			return;
		}

		int runtimeId = TunnelMC.mc.player.getId();

		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		movePlayerPacket.setRuntimeEntityId(runtimeId);
		movePlayerPacket.setPosition(Vector3f.from(currentPosX, currentPosY, currentPosZ));
		movePlayerPacket.setRotation(Vector3f.from(currentPitch, currentYaw, currentYaw)); // Set yaw twice so BDS cooperates with head movement better
		movePlayerPacket.setMode(mode);
		movePlayerPacket.setOnGround(currentlyOnGround);
		Client.instance.sendPacket(movePlayerPacket);

		PlayerMoveTranslator.lastPosX = currentPosX;
		PlayerMoveTranslator.lastPosY = currentPosY;
		PlayerMoveTranslator.lastPosZ = currentPosZ;
		PlayerMoveTranslator.lastYaw = currentYaw;
		PlayerMoveTranslator.lastPitch = currentPitch;
		PlayerMoveTranslator.lastOnGround = currentlyOnGround;
	}

}
