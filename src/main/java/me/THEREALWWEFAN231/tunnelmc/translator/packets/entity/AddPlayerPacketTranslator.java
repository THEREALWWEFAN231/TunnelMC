package me.THEREALWWEFAN231.tunnelmc.translator.packets.entity;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.nukkitx.protocol.bedrock.packet.AddPlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.util.math.Vec3d;

public class AddPlayerPacketTranslator extends PacketTranslator<AddPlayerPacket> {

	@Override
	public void translate(AddPlayerPacket packet) {
		int id = (int) packet.getRuntimeEntityId();
		UUID uuid = packet.getUuid();
		String name = packet.getUsername();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY();
		double z = packet.getPosition().getZ();
		float pitch = packet.getRotation().getX();//TODO: not sure about these
		float yaw = packet.getRotation().getY();
		Vec3d velocity = new Vec3d(packet.getMotion().getX(), packet.getMotion().getY(), packet.getMotion().getZ());

		OtherClientPlayerEntity player = new OtherClientPlayerEntity(TunnelMC.mc.world, new GameProfile(uuid, name));
		player.setEntityId(id);
		player.setPos(x, y, z);
		player.yaw = yaw;
		player.pitch = pitch;
		player.setVelocity(velocity);

		PlayerSpawnS2CPacket playerSpawnS2CPacket = new PlayerSpawnS2CPacket(player);
		Client.instance.javaConnection.processServerToClientPacket(playerSpawnS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return AddPlayerPacket.class;
	}

}
