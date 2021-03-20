package me.THEREALWWEFAN231.tunnelmc.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.nukkitx.protocol.bedrock.BedrockPacket;

import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.*;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.entity.*;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.inventory.ContainerOpenPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.inventory.InventoryContentPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.inventory.InventorySlotPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.world.*;

public class PacketTranslatorManager {

	private final ArrayList<PacketTranslator<?>> packetTranslators = new ArrayList<>();
	private final HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByClass = new HashMap<>();
	private final HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByPacketClass = new HashMap<>();

	private final CopyOnWriteArrayList<IdleThing> idlePackets = new CopyOnWriteArrayList<>();//I didn't actually check if I need CopyOnWriteArrayList, but I assume so

	public PacketTranslatorManager() {
		this.addTranslator(new StartGameTranslator());
		this.addTranslator(new ChunkRadiusUpdatedPacketTranslator());
		this.addTranslator(new LevelChunkPacketTranslator());
		this.addTranslator(new PlayStatusPacketTranslator());
		this.addTranslator(new ResourcePacksInfoPacketTranslator());
		this.addTranslator(new ResourcePackStackPacketTranslator());
		this.addTranslator(new AddPlayerPacketTranslator());
		this.addTranslator(new PlayerListPacketTranslator());
		this.addTranslator(new TextTranslator());
		this.addTranslator(new AddEntityPacketTranslator());
		this.addTranslator(new SetTimePacketTranslator());
		this.addTranslator(new RemoveEntityPacketTranslator());
		this.addTranslator(new InventorySlotPacketTranslator());
		this.addTranslator(new AddItemEntityPacketTranslator());
		this.addTranslator(new MovePlayerPacketTranslator());
		this.addTranslator(new MoveEntityAbsolutePacketTranslator());
		this.addTranslator(new ServerToClientHandshakePacketTranslator());
		this.addTranslator(new UpdateBlockTranslator());
		this.addTranslator(new SetEntityMotionPacketTranslator());
		this.addTranslator(new TakeItemEntityPacketTranslator());
		this.addTranslator(new NetworkChunkPublisherUpdatePacketTranslator());
		this.addTranslator(new SetEntityDataPacketTranslator());
		this.addTranslator(new ContainerOpenPacketTranslator());
		this.addTranslator(new InventoryContentPacketTranslator());
		this.addTranslator(new DisconnectPacketTranslator());
		this.addTranslator(new UpdatePlayerGameTypeTranslator());
		this.addTranslator(new AdventureSettingsTranslator());
		this.addTranslator(new AnimateTranslator());
		
		EventManager.register(this);
	}

	private void addTranslator(PacketTranslator<?> translator) {
		this.packetTranslators.add(translator);
		this.packetTranslatorsByClass.put(translator.getClass(), translator);
		this.packetTranslatorsByPacketClass.put(translator.getPacketClass(), translator);
	}

	@SuppressWarnings("unchecked")
	public void translatePacket(BedrockPacket bedrockPacket) {
		PacketTranslator<BedrockPacket> packetTranslator = (PacketTranslator<BedrockPacket>) this.packetTranslatorsByPacketClass.get(bedrockPacket.getClass());
		if (packetTranslator != null) {
			if (!packetTranslator.idleUntil()) {//if false we have to wait
				this.idlePackets.add(new IdleThing(packetTranslator, bedrockPacket));
				return;
			}

			packetTranslator.translate(bedrockPacket);
		} else {
			//System.out.println("Could not find a packet translator for the packet: " + bedrockPacket.getClass());
		}
	}

	@EventTarget
	public void onEvent(EventPlayerTick event) {
		for (int i = 0; i < this.idlePackets.size(); i++) {
			IdleThing idleThing = this.idlePackets.get(i);
			if (!idleThing.packetTranslator.idleUntil()) {//still need to wait
				continue;
			}

			idleThing.getPacketTranslator().translate(idleThing.getBedrockPacket());
			this.idlePackets.remove(i);
			i--;
		}
	}

	private static class IdleThing {
		private final PacketTranslator<BedrockPacket> packetTranslator;
		private final BedrockPacket bedrockPacket;

		public IdleThing(PacketTranslator<BedrockPacket> packetTranslator, BedrockPacket bedrockPacket) {
			this.packetTranslator = packetTranslator;
			this.bedrockPacket = bedrockPacket;
		}

		public PacketTranslator<BedrockPacket> getPacketTranslator() {
			return this.packetTranslator;
		}

		public BedrockPacket getBedrockPacket() {
			return this.bedrockPacket;
		}
	}

}
