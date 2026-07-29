package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class SetEntityMotionTranslator extends PacketTranslator<SetEntityMotionPacket>{

	@Override
	public void translate(SetEntityMotionPacket packet) {
		if (FunnelMC.mc.level == null) {
			// Stack trace thrown otherwise
			return;
		}
		int id = (int) packet.getRuntimeEntityId();
		Vec3 velocity = new Vec3(packet.getMotion().getX(), packet.getMotion().getY(), packet.getMotion().getZ());

		ClientboundSetEntityMotionPacket entityVelocityUpdateS2CPacket = new ClientboundSetEntityMotionPacket(id, velocity);
		Client.instance.javaConnection.processServerToClientPacket(entityVelocityUpdateS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return SetEntityMotionPacket.class;
	}

}
