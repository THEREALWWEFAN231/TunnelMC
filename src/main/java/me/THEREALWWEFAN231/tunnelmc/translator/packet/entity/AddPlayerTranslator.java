package me.THEREALWWEFAN231.tunnelmc.translator.packet.entity;

import java.util.Collections;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.nukkitx.protocol.bedrock.packet.AddPlayerPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.util.math.Vec3d;

public class AddPlayerTranslator extends PacketTranslator<AddPlayerPacket> {

	@Override
	public void translate(AddPlayerPacket packet) {
		if (TunnelMC.mc.world == null) {
			return;
		}

		int id = (int) packet.getRuntimeEntityId();
		UUID uuid = packet.getUuid();
		String name = packet.getUsername();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY();
		double z = packet.getPosition().getZ();
		float pitch = packet.getRotation().getX();
		float yaw = packet.getRotation().getY();
		Vec3d velocity = new Vec3d(packet.getMotion().getX(), packet.getMotion().getY(), packet.getMotion().getZ());

		Runnable runnable = () -> {
			OtherClientPlayerEntity player = new OtherClientPlayerEntity(TunnelMC.mc.world, new GameProfile(uuid, name));
			player.setEntityId(id);
			player.setPos(x, y, z);
			player.yaw = yaw;
			player.pitch = pitch;
			player.setVelocity(velocity);

			PlayerSpawnS2CPacket playerSpawnS2CPacket = new PlayerSpawnS2CPacket(player);
			Client.instance.javaConnection.processServerToClientPacket(playerSpawnS2CPacket);

			Pair<EquipmentSlot, ItemStack> itemStackPair = new Pair<>(EquipmentSlot.MAINHAND, ItemTranslator.itemDataToItemStack(packet.getHand()));
			EntityEquipmentUpdateS2CPacket equipmentUpdatePacket = new EntityEquipmentUpdateS2CPacket((int) packet.getRuntimeEntityId(),
					Collections.singletonList(itemStackPair));
			Client.instance.javaConnection.processServerToClientPacket(equipmentUpdatePacket);
		};

		if (TunnelMC.mc.world != null) {
			runnable.run();
		} else {
			MinecraftClient.getInstance().execute(runnable);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return AddPlayerPacket.class;
	}

}