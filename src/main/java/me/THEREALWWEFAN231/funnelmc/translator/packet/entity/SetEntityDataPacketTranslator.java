package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.EnumMap;
import java.util.List;

public class SetEntityDataPacketTranslator extends PacketTranslator<SetEntityDataPacket> {
	@Override
	public void translate(SetEntityDataPacket packet) {

		//TODO: Set up an entity class system, like Geyser?
		int id = (int) packet.getRuntimeEntityId();

		if (FunnelMC.mc.level != null) {
			Entity entity = FunnelMC.mc.level.getEntity(id);
			if (entity == null) {
				//System.out.println("No entity found with ID " + id);
				return;
			}
			EntityDataMap metadata = packet.getMetadata();

			if (metadata.containsKey(EntityDataTypes.AIR_SUPPLY)) {
				entity.setAirSupply(metadata.get(EntityDataTypes.AIR_SUPPLY));
			}
			// TODO: health is no longer part of the entity metadata map in this protocol version;
			// it now needs to be sourced from a different packet (e.g. attributes).

			EnumMap<EntityFlag, Boolean> flags = metadata.getFlags();

			if (flags != null) {
				boolean sneaking = metadata.getFlag(EntityFlag.SNEAKING);
				entity.setShiftKeyDown(sneaking);
				entity.setPose(sneaking ? Pose.CROUCHING : Pose.STANDING);
			}

			// packDirty() returns null (not an empty list) when nothing was actually marked dirty
			// above - e.g. a SetEntityDataPacket that only carried keys we don't translate yet.
			// ClientboundSetEntityDataPacket's own handler assumes a real server would only ever send
			// one when there's something to pack, so it iterates the list with no null check.
			List<SynchedEntityData.DataValue<?>> dirty = entity.getEntityData().packDirty();
			if (dirty != null) {
				ClientboundSetEntityDataPacket trackerUpdatePacket = new ClientboundSetEntityDataPacket(id, dirty);
				Client.instance.javaConnection.processServerToClientPacket(trackerUpdatePacket);
			}
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return SetEntityDataPacket.class;
	}

}
