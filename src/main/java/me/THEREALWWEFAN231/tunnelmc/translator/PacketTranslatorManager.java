package me.THEREALWWEFAN231.tunnelmc.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.nukkitx.protocol.bedrock.BedrockPacket;

import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.*;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.entity.*;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.inventory.ContainerOpenPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.inventory.InventoryContentPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.inventory.InventorySlotPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.world.*;

public class PacketTranslatorManager {

	private final Map<Class<?>, PacketTranslator<?>> packetTranslatorsByPacketClass = new HashMap<>();

	private final CopyOnWriteArrayList<IdleThing> idlePackets = new CopyOnWriteArrayList<>();//I didn't actually check if I need CopyOnWriteArrayList, but I assume so

	public PacketTranslatorManager() {
		this.addTranslator(new StartGameTranslator());
		this.addTranslator(new ChunkRadiusUpdatedTranslator());
		this.addTranslator(new LevelChunkTranslator());
		this.addTranslator(new PlayStatusPacketTranslator());
		this.addTranslator(new ResourcePacksInfoPacketTranslator());
		this.addTranslator(new ResourcePackStackPacketTranslator());
		this.addTranslator(new AddPlayerTranslator());
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
		this.addTranslator(new SetEntityMotionTranslator());
		this.addTranslator(new TakeItemEntityPacketTranslator());
		this.addTranslator(new NetworkChunkPublisherUpdateTranslator());
		this.addTranslator(new SetEntityDataPacketTranslator());
		this.addTranslator(new ContainerOpenPacketTranslator());
		this.addTranslator(new InventoryContentPacketTranslator());
		this.addTranslator(new DisconnectTranslator());
		this.addTranslator(new SetPlayerGameTypeTranslator());
		this.addTranslator(new AdventureSettingsTranslator());
		this.addTranslator(new AnimateTranslator());
		this.addTranslator(new MobEquipmentTranslator());
		this.addTranslator(new MobArmorEquipmentTranslator());
		this.addTranslator(new BlockEntityDataTranslator());
		this.addTranslator(new GameRulesChangedTranslator());
		this.addTranslator(new UpdatePlayerGameTypeTranslator());
		this.addTranslator(new LevelEventTranslator());
		this.addTranslator(new LevelSoundEvent2Translator());
		this.addTranslator(new LevelSoundEventTranslator());
		this.addTranslator(new BlockEntityDataPacketTranslator());
		
		EventManager.register(this);
	}

	private void addTranslator(PacketTranslator<?> translator) {
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
