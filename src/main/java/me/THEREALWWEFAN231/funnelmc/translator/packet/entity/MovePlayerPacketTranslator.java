package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

import java.util.Collections;

public class MovePlayerPacketTranslator extends PacketTranslator<MovePlayerPacket> {

	private static final Logger logger = LogManager.getLogger(MovePlayerPacketTranslator.class);

	// Server-authoritative movement echoes MovePlayerPacket for our own id essentially every tick
	// to confirm/correct our reported position - that's normal and a real Bedrock client just
	// reconciles silently. Forcing every single echo into a ClientboundPlayerPositionPacket (a hard
	// teleport that also zeroes velocity) freezes local prediction ~20 times a second even when the
	// echoed position matches what we already predicted. Only actually correct when the server's
	// position has drifted from ours by more than this many blocks.
	private static final double POSITION_CORRECTION_THRESHOLD = 0.5;

	@Override
	public void translate(MovePlayerPacket packet) {

		int id = (int) packet.getRuntimeEntityId();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY() - FunnelMC.mc.player.getEyeHeight(Pose.STANDING);
		double z = packet.getPosition().getZ();

		float yaw = packet.getRotation().getY();
		byte packedYaw = (byte) ((int) (yaw * 256.0F / 360.0F));
		float pitch = packet.getRotation().getX();
		boolean onGround = packet.isOnGround();

		if (id == FunnelMC.mc.player.getId()) {
			double drift = FunnelMC.mc.player.position().distanceToSqr(x, y, z);
			logger.warn("[MovementDiag] own-player MovePlayerPacket pos=({}, {}, {}) driftSqr={}", x, y, z, drift);
			if (drift <= POSITION_CORRECTION_THRESHOLD * POSITION_CORRECTION_THRESHOLD) {
				return;
			}
			PositionMoveRotation positionMoveRotation = new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, yaw, pitch);
			ClientboundPlayerPositionPacket positionPacket = new ClientboundPlayerPositionPacket(0, positionMoveRotation, Collections.emptySet());
			Client.instance.javaConnection.processServerToClientPacket(positionPacket);
			return;
		}

		PositionMoveRotation positionMoveRotation = new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, yaw, pitch);
		ClientboundTeleportEntityPacket clientboundTeleportEntityPacket = new ClientboundTeleportEntityPacket(id, positionMoveRotation, Collections.<Relative>emptySet(), onGround);

		Client.instance.javaConnection.processServerToClientPacket(clientboundTeleportEntityPacket);

		Entity entity = FunnelMC.mc.level != null ? FunnelMC.mc.level.getEntity(id) : null;
		if (entity != null) {
			ClientboundRotateHeadPacket clientboundRotateHeadPacket = new ClientboundRotateHeadPacket(entity, packedYaw);
			Client.instance.javaConnection.processServerToClientPacket(clientboundRotateHeadPacket);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return MovePlayerPacket.class;
	}

	@Override
	public boolean idleUntil() {
		return FunnelMC.mc.player != null;
	}

}
