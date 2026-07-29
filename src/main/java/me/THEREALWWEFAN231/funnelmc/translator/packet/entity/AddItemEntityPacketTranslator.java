package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import java.util.UUID;

import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.item.ItemTranslator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AddItemEntityPacketTranslator extends PacketTranslator<AddItemEntityPacket> {

	@Override
	public void translate(AddItemEntityPacket packet) {

		int id = (int) packet.getUniqueEntityId();
		double x = packet.getPosition().getX();
		double y = packet.getPosition().getY();
		double z = packet.getPosition().getZ();
		double motionX = packet.getMotion().getX();
		double motionY = packet.getMotion().getY();
		double motionZ = packet.getMotion().getZ();

		EntityType<?> itemEntityType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("item"));
		ItemEntity itemEntity = (ItemEntity) itemEntityType.create(FunnelMC.mc.level, EntitySpawnReason.LOAD);
		itemEntity.setId(id);
		itemEntity.setPos(x, y, z);
		itemEntity.setDeltaMovement(motionX, motionY, motionZ);
		itemEntity.setItem(ItemTranslator.itemDataToItemStack(packet.getItemInHand()));
		itemEntity.setUUID(UUID.randomUUID());

		ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(id, itemEntity.getUUID(), x, y, z, 0, 0, itemEntityType, 0, new Vec3(motionX, motionY, motionZ), 0);
		Client.instance.javaConnection.processServerToClientPacket(addEntityPacket);

		// packDirty() returns null (not an empty list) when nothing was actually marked dirty -
		// e.g. an item stack that translates to the default (empty) value. ClientboundSetEntityDataPacket's
		// own handler assumes a real server would only ever send one when there's something to pack,
		// so it iterates the list with no null check.
		List<SynchedEntityData.DataValue<?>> dirty = itemEntity.getEntityData().packDirty();
		if (dirty != null) {
			ClientboundSetEntityDataPacket entityTrackerUpdateS2CPacket = new ClientboundSetEntityDataPacket(id, dirty);
			Client.instance.javaConnection.processServerToClientPacket(entityTrackerUpdateS2CPacket);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return AddItemEntityPacket.class;
	}

}
