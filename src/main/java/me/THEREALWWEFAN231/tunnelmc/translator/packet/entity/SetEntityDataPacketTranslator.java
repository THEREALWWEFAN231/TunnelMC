package me.THEREALWWEFAN231.tunnelmc.translator.packet.entity;

import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlags;
import com.nukkitx.protocol.bedrock.packet.SetEntityDataPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;

public class SetEntityDataPacketTranslator extends PacketTranslator<SetEntityDataPacket> {
	@Override
	public void translate(SetEntityDataPacket packet) {

		//TODO: Set up an entity class system, like Geyser?
		int id = (int) packet.getRuntimeEntityId();

		if (TunnelMC.mc.world != null) {
			Entity entity = TunnelMC.mc.world.getEntityById(id);
			if (entity == null) {
				//System.out.println("No entity found with ID " + id);
				return;
			}
			EntityDataMap metadata = packet.getMetadata();

			if (metadata.containsKey(EntityData.AIR_SUPPLY)) {
				entity.setAir(metadata.getShort(EntityData.AIR_SUPPLY));
			} else if (metadata.containsKey(EntityData.HEALTH)) {
				if (entity instanceof LivingEntity) {
					((LivingEntity) entity).setHealth(metadata.getInt(EntityData.HEALTH));
				}
			}

			EntityFlags flags = metadata.getFlags();

			if (flags != null) {
				entity.setSneaking(flags.getFlag(EntityFlag.SNEAKING));

				if (flags.getFlag(EntityFlag.SNEAKING)) {
					entity.setPose(EntityPose.CROUCHING);
				} else {
					entity.setPose(EntityPose.STANDING);
				}
			}

			EntityTrackerUpdateS2CPacket trackerUpdatePacket = new EntityTrackerUpdateS2CPacket(id, entity.getDataTracker(), true);
			Client.instance.javaConnection.processServerToClientPacket(trackerUpdatePacket);
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return SetEntityDataPacket.class;
	}

}
