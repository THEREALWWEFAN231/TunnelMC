package me.THEREALWWEFAN231.funnelmc.javaconnection.packet.movement;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class PlayerMoveTranslator extends PacketTranslator<ServerboundMovePlayerPacket> {

	//so java edition sends movement packets every x(i forgot) ticks  even if we didn't move, bedrock doesn't do this, so we basically try to ignore these packets
	public static double lastPosX;
	public static double lastPosY = -Double.MAX_VALUE;//just incase we for some reason spawn at 0 0 0, with 0 ayw and 0 pitch :flushed: even though that shouldn't be a problem
	public static double lastPosZ;
	public static float lastYaw;
	public static float lastPitch;
	public static boolean lastOnGround;

	@Override
	public void translate(ServerboundMovePlayerPacket packet) {
		//this shouldn't even be called? I don't know, doesn't matter
		PlayerMoveTranslator.translateMovementPacket(packet, MovePlayerPacket.Mode.NORMAL);
	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundMovePlayerPacket.class;
	}

	public static void translateMovementPacket(ServerboundMovePlayerPacket serverboundMovePlayerPacket, MovePlayerPacket.Mode mode) {
		// Servers running server-authoritative movement (most current ones, including the vanilla
		// Bedrock Dedicated Server by default) reject MovePlayerPacket outright - PlayerAuthInputSender
		// reports position/rotation instead, every tick, via PlayerAuthInputPacket.
		if (Client.instance.movementMode != AuthoritativeMovementMode.CLIENT) {
			return;
		}

		double currentPosX = serverboundMovePlayerPacket.getX(FunnelMC.mc.player.getX());
		double currentPosY = serverboundMovePlayerPacket.getY(FunnelMC.mc.player.getY()) + FunnelMC.mc.player.getEyeHeight(Pose.STANDING);
		double currentPosZ = serverboundMovePlayerPacket.getZ(FunnelMC.mc.player.getZ());
		float currentYaw = serverboundMovePlayerPacket.getYRot(FunnelMC.mc.player.getYRot());
		float currentPitch = serverboundMovePlayerPacket.getXRot(FunnelMC.mc.player.getXRot());
		boolean currentlyOnGround = serverboundMovePlayerPacket.isOnGround();

		if (PlayerMoveTranslator.lastPosX == currentPosX && PlayerMoveTranslator.lastPosY == currentPosY && PlayerMoveTranslator.lastPosZ == currentPosZ && PlayerMoveTranslator.lastYaw == currentYaw && PlayerMoveTranslator.lastPitch == currentPitch && PlayerMoveTranslator.lastOnGround == currentlyOnGround) {
			return;
		}

		int runtimeId = FunnelMC.mc.player.getId();

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
