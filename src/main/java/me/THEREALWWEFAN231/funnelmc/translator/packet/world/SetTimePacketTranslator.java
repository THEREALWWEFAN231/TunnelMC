package me.THEREALWWEFAN231.funnelmc.translator.packet.world;

import java.util.Collections;

import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class SetTimePacketTranslator extends PacketTranslator<SetTimePacket> {

	@Override
	public void translate(SetTimePacket packet) {
		// TODO: the old (long gameTime, long dayTime, boolean doDaylightCycle) signature is gone -
		// modern MC drives day/night via a WorldClock registry (part of the same environment
		// attribute system as DimensionType, see DimensionTranslator TODO), passed here as a
		// Map<Holder<WorldClock>, ClockNetworkState> we don't have real data for yet.
		ClientboundSetTimePacket clientboundSetTimePacket = new ClientboundSetTimePacket(packet.getTime(), Collections.emptyMap());
		Client.instance.javaConnection.processServerToClientPacket(clientboundSetTimePacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return SetTimePacket.class;
	}

	@Override
	public boolean idleUntil() {
		return FunnelMC.mc.level != null;
	}

}
