package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.SetEntityMotionPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

public class SetEntityMotionPacketTranslator extends PacketTranslator<SetEntityMotionPacket>{

	@Override
	public void translate(SetEntityMotionPacket packet) {
		
		int id = (int) packet.getRuntimeEntityId();
		Vec3d velocity = new Vec3d(packet.getMotion().getX(), packet.getMotion().getY(), packet.getMotion().getZ());
		
		EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket = new EntityVelocityUpdateS2CPacket(id, velocity);
		Client.instance.javaConnection.processServerToClientPacket(entityVelocityUpdateS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return SetEntityMotionPacket.class;
	}

}
