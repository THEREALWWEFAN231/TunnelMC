package me.THEREALWWEFAN231.tunnelmc.translator.packets.entity;

import com.nukkitx.protocol.bedrock.packet.AddEntityPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.EntityTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class AddEntityPacketTranslator extends PacketTranslator<AddEntityPacket> {
	
	//TODO: handle non living entities differently, EntitySpawnS2CPacket
	
	@SuppressWarnings("unchecked")
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
			
			Entity entity = entityType.create(TunnelMC.mc.world);
			entity.setEntityId(id);
			entity.setPos(x, y, z);
			entity.setVelocity(motionX, motionY, motionZ);
			entity.yaw = yaw;
			entity.pitch = pitch;
			
			Client.instance.javaConnection.processServerToClientPacket((Packet<ClientPlayPacketListener>) entity.createSpawnPacket());
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return AddEntityPacket.class;
	}

}
