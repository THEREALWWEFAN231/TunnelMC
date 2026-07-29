package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import java.util.Collections;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.item.ItemTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class AddPlayerTranslator extends PacketTranslator<AddPlayerPacket> {

	@Override
	public void translate(AddPlayerPacket packet) {
		int id = (int) packet.getRuntimeEntityId();
		UUID uuid = packet.getUuid();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY();
		double z = packet.getPosition().getZ();
		float pitch = packet.getRotation().getX();//TODO: not sure about these
		float yaw = packet.getRotation().getY();
		Vec3 velocity = new Vec3(packet.getMotion().getX(), packet.getMotion().getY(), packet.getMotion().getZ());

		// The client itself spawns a RemotePlayer for this entity as long as a matching
		// ClientboundPlayerInfoUpdatePacket (see PlayerListPacketTranslator) with this UUID has
		// already been sent, exactly like the vanilla protocol expects.
		EntityType<?> playerEntityType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("player"));

		Runnable runnable = () -> {
			ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(id, uuid, x, y, z, pitch, yaw, playerEntityType, 0, velocity, yaw);
			Client.instance.javaConnection.processServerToClientPacket(addEntityPacket);

			Pair<EquipmentSlot, ItemStack> itemStackPair = new Pair<>(EquipmentSlot.MAINHAND, ItemTranslator.itemDataToItemStack(packet.getHand()));
			ClientboundSetEquipmentPacket equipmentUpdatePacket = new ClientboundSetEquipmentPacket(id, Collections.singletonList(itemStackPair));
			Client.instance.javaConnection.processServerToClientPacket(equipmentUpdatePacket);
		};
		if (FunnelMC.mc.level != null) {
			runnable.run();
		} else {
			Minecraft.getInstance().execute(runnable);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return AddPlayerPacket.class;
	}

}
