package me.THEREALWWEFAN231.tunnelmc.javaconnection;

import java.util.HashMap;

import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.ChatMessageC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.HandSwingC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.PlayerActionC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.PlayerInteractBlockC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.PlayerInteractEntityC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.PlayerInteractItemC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.UpdateSelectedSlotC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement.BothTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement.LookOnlyTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement.PlayerMoveC2SPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators.movement.PositionOnlyTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.Packet;

public class JavaPacketTranslatorManager {

	private HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByPacketClass = new HashMap<Class<?>, PacketTranslator<?>>();

	public JavaPacketTranslatorManager() {
		this.addTranslator(new HandSwingC2SPacketTranslator());
		this.addTranslator(new PlayerMoveC2SPacketTranslator());
		this.addTranslator(new LookOnlyTranslator());
		this.addTranslator(new PositionOnlyTranslator());
		this.addTranslator(new BothTranslator());
		this.addTranslator(new ChatMessageC2SPacketTranslator());
		this.addTranslator(new UpdateSelectedSlotC2SPacketTranslator());
		this.addTranslator(new PlayerActionC2SPacketTranslator());
		this.addTranslator(new PlayerInteractBlockC2SPacketTranslator());
		this.addTranslator(new PlayerInteractItemC2SPacketTranslator());
		this.addTranslator(new PlayerInteractEntityC2SPacketTranslator());
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
