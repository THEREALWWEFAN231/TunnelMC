package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.util.math.MathHelper;

public class PlayerMoveC2SPacketTranslator extends PacketTranslator<PlayerMoveC2SPacket> {

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
		PlayerMoveC2SPacketTranslator.translateMovementPacket(packet, MovePlayerPacket.Mode.NORMAL);
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerMoveC2SPacket.class;
	}

	public static void translateMovementPacket(PlayerMoveC2SPacket playerMoveC2SPacket, MovePlayerPacket.Mode mode) {
		double currentPosX = playerMoveC2SPacket.getX(TunnelMC.mc.player.getPos().x);
		double currentPosY = playerMoveC2SPacket.getY(TunnelMC.mc.player.getPos().y) + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING);
		double currentPosZ = playerMoveC2SPacket.getZ(TunnelMC.mc.player.getPos().z);
		float currentYaw = playerMoveC2SPacket.getYaw(TunnelMC.mc.player.yaw);
		float currentPitch = playerMoveC2SPacket.getPitch(TunnelMC.mc.player.pitch);
		boolean currentlyOnGround = playerMoveC2SPacket.isOnGround();

		if (PlayerMoveC2SPacketTranslator.lastPosX == currentPosX && PlayerMoveC2SPacketTranslator.lastPosY == currentPosY
				&& PlayerMoveC2SPacketTranslator.lastPosZ == currentPosZ && PlayerMoveC2SPacketTranslator.lastYaw == currentYaw
				&& PlayerMoveC2SPacketTranslator.lastPitch == currentPitch && PlayerMoveC2SPacketTranslator.lastOnGround == currentlyOnGround) {
			return;
		}

		int runtimeId = TunnelMC.mc.player.getEntityId();

		MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
		movePlayerPacket.setRuntimeEntityId(runtimeId);
		movePlayerPacket.setPosition(Vector3f.from(currentPosX, currentPosY, currentPosZ));
		movePlayerPacket.setRotation(Vector3f.from(currentPitch, currentYaw, 0));
		movePlayerPacket.setMode(mode);//honestly we could just check if the playerMoveC2SPacket is a rotation packet, and if it is use HEAD_ROTATION
		movePlayerPacket.setOnGround(currentlyOnGround);
		Client.instance.sendPacket(movePlayerPacket);

		PlayerMoveC2SPacketTranslator.lastPosX = currentPosX;
		PlayerMoveC2SPacketTranslator.lastPosY = currentPosY;
		PlayerMoveC2SPacketTranslator.lastPosZ = currentPosZ;
		PlayerMoveC2SPacketTranslator.lastYaw = currentYaw;
		PlayerMoveC2SPacketTranslator.lastPitch = currentPitch;
		PlayerMoveC2SPacketTranslator.lastOnGround = currentlyOnGround;

//		//update our "chunk radius center" every time we move
//		int chunkX = MathHelper.floor(currentPosX) >> 4;
//		int chunkZ = MathHelper.floor(currentPosZ) >> 4;
//		ChunkRenderDistanceCenterS2CPacket chunkRenderDistanceCenterS2CPacket = new ChunkRenderDistanceCenterS2CPacket(chunkX, chunkZ);
//		Client.instance.javaConnection.processServerToClientPacket(chunkRenderDistanceCenterS2CPacket);
	}

}
