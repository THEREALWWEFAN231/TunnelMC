package me.THEREALWWEFAN231.tunnelmc.translator.packet.entity;

import java.util.UUID;

import com.nukkitx.protocol.bedrock.packet.AddItemEntityPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;

public class AddItemEntityPacketTranslator extends PacketTranslator<AddItemEntityPacket> {

	@SuppressWarnings("unchecked")
	@Override
	public void translate(AddItemEntityPacket packet) {
		int id = (int) packet.getUniqueEntityId();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY();
		double z = packet.getPosition().getZ();
		double motionX = packet.getMotion().getX();
		double motionY = packet.getMotion().getY();
		double motionZ = packet.getMotion().getZ();
		
		EntityType<ItemEntity> entityType = EntityType.ITEM;
		ItemEntity itemEntity = entityType.create(TunnelMC.mc.world);
		itemEntity.setEntityId(id);
		itemEntity.setPos(x, y, z);
		itemEntity.setVelocity(motionX, motionY, motionZ);
		itemEntity.setStack(ItemTranslator.itemDataToItemStack(packet.getItemInHand()));
		itemEntity.setUuid(UUID.randomUUID());
		
		Client.instance.javaConnection.processServerToClientPacket((Packet<ClientPlayPacketListener>) itemEntity.createSpawnPacket());
		
		DataTracker dataTracker = itemEntity.getDataTracker();
		EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket = new EntityTrackerUpdateS2CPacket(id, dataTracker, false);
		Client.instance.javaConnection.processServerToClientPacket(entityTrackerUpdateS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return AddItemEntityPacket.class;
	}

}