package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;

import java.util.Collections;

public class MoveEntityAbsolutePacketTranslator extends PacketTranslator<MoveEntityAbsolutePacket> {

	@Override
	public void translate(MoveEntityAbsolutePacket packet) {

		int id = (int) packet.getRuntimeEntityId();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY();
		double z = packet.getPosition().getZ();

		float yaw = packet.getRotation().getY();
		byte packedYaw = (byte) ((int) (yaw * 256.0F / 360.0F));
		float pitch = packet.getRotation().getX();
		boolean onGround = packet.isOnGround();

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
		return MoveEntityAbsolutePacket.class;
	}

}
