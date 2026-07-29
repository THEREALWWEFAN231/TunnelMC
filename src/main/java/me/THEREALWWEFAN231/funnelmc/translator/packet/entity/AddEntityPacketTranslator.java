package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import java.util.UUID;

import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.EntityTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;

public class AddEntityPacketTranslator extends PacketTranslator<AddEntityPacket> {

	//TODO: handle non living entities differently, EntitySpawnS2CPacket

	@Override
	public void translate(AddEntityPacket packet) {

		EntityType<?> entityType = EntityTranslator.BEDROCK_IDENTIFIER_TO_ENTITY_TYPE.get(packet.getIdentifier());
		if (entityType == null) {
			System.out.println("Could not find entity type " + packet.getIdentifier());
			return;
		} else {

			int id = (int) packet.getUniqueEntityId();
			double x = packet.getPosition().getX();
			double y = packet.getPosition().getY();
			double z = packet.getPosition().getZ();
			double motionX = packet.getMotion().getX();
			double motionY = packet.getMotion().getY();
			double motionZ = packet.getMotion().getZ();
			float yaw = packet.getRotation().getY();//TODO: not sure about these
			float pitch = packet.getRotation().getX();

			ClientboundAddEntityPacket clientboundAddEntityPacket = new ClientboundAddEntityPacket(id, UUID.randomUUID(), x, y, z, pitch, yaw, entityType, 0, new Vec3(motionX, motionY, motionZ), yaw);

			Client.instance.javaConnection.processServerToClientPacket(clientboundAddEntityPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return AddEntityPacket.class;
	}

}
