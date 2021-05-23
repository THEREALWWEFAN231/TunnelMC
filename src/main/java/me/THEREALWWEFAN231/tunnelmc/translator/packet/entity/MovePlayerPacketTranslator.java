package me.THEREALWWEFAN231.tunnelmc.translator.packet.entity;

import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.mixins.interfaces.IMixinEntityPositionS2CPacket;
import me.THEREALWWEFAN231.tunnelmc.mixins.interfaces.IMixinEntitySetHeadYawS2CPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class MovePlayerPacketTranslator extends PacketTranslator<MovePlayerPacket> {
	private static final AtomicInteger teleportId = new AtomicInteger(1);

	@Override
	public void translate(MovePlayerPacket packet) {

		int id = (int) packet.getRuntimeEntityId();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY() - TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING);
		double z = packet.getPosition().getZ();

		float realYaw = packet.getRotation().getY();
		byte yaw = (byte) ((int) (realYaw * 256.0F / 360.0F));
		float realPitch = packet.getRotation().getX();
		byte pitch = (byte) ((int) (realPitch * 256.0F / 360.0F));
		boolean onGround = packet.isOnGround();

		if (id == TunnelMC.mc.player.getEntityId()) {
			// This works best
			PlayerPositionLookS2CPacket positionPacket = new PlayerPositionLookS2CPacket(x, y, z, yaw, pitch, Collections.emptySet(), teleportId.getAndIncrement());
			Client.instance.javaConnection.processServerToClientPacket(positionPacket);
			return;
		}

		EntityPositionS2CPacket entityPositionS2CPacket = new EntityPositionS2CPacket();
		IMixinEntityPositionS2CPacket iMixinEntityPositionS2CPacket = (IMixinEntityPositionS2CPacket) entityPositionS2CPacket;

		iMixinEntityPositionS2CPacket.setId(id);
		iMixinEntityPositionS2CPacket.setX(x);
		iMixinEntityPositionS2CPacket.setY(y);
		iMixinEntityPositionS2CPacket.setZ(z);
		iMixinEntityPositionS2CPacket.setYaw(yaw);
		iMixinEntityPositionS2CPacket.setPitch(pitch);
		iMixinEntityPositionS2CPacket.setOnGround(onGround);

		Client.instance.javaConnection.processServerToClientPacket(entityPositionS2CPacket);

		EntitySetHeadYawS2CPacket entitySetHeadYawS2CPacket = new EntitySetHeadYawS2CPacket();
		IMixinEntitySetHeadYawS2CPacket iMixinEntitySetHeadYawS2CPacket = (IMixinEntitySetHeadYawS2CPacket) entitySetHeadYawS2CPacket;

		iMixinEntitySetHeadYawS2CPacket.setEntityId(id);
		iMixinEntitySetHeadYawS2CPacket.setYaw(yaw);

		Client.instance.javaConnection.processServerToClientPacket(entitySetHeadYawS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return MovePlayerPacket.class;
	}
	
	@Override
	public boolean idleUntil() {
		return TunnelMC.mc.player == null;
	}

}
