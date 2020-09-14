package me.THEREALWWEFAN231.tunnelmc.javaconnection;

import java.util.HashMap;

import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.ChatMessageC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.HandSwingC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.PlayerMoveC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.Packet;

public class JavaPacketTranslatorManager {

	private HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByPacketClass = new HashMap<Class<?>, PacketTranslator<?>>();

	public JavaPacketTranslatorManager() {
		this.addTranslator(new HandSwingC2SPacketTranslator());
		this.addTranslator(new PlayerMoveC2SPacketTranslator());
		this.addTranslator(new ChatMessageC2SPacketTranslator());
	}

	private void addTranslator(PacketTranslator<?> translator) {
		this.packetTranslatorsByPacketClass.put(translator.getPacketClass(), translator);
	}

	public void translatePacket(Packet<?> packet) {
		PacketTranslator packetTranslator = this.packetTranslatorsByPacketClass.get(packet.getClass());
		if (packetTranslator != null) {
			packetTranslator.translate(packet);
		} else {
			//System.out.println("Could not find a packet translator for the packet: " + packet.getClass());
		}
	}

}
