package me.THEREALWWEFAN231.tunnelmc.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.nukkitx.protocol.bedrock.BedrockPacket;

import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.AddEntityPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.AddItemEntityPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.AddPlayerPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.ChunkRadiusUpdatedPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.ContainerOpenPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.InventoryContentPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.InventorySlotPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.LevelChunkPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.MoveEntityAbsolutePacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.MovePlayerPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.NetworkChunkPublisherUpdatePacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.PlayStatusPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.PlayerListPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.RemoveEntityPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.ResourcePackStackPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.ResourcePacksInfoPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.ServerToClientHandshakePacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.SetEntityDataPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.SetEntityMotionPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.SetTimePacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.StartGamePacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.TakeItemEntityPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.TextPacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.UpdateBlockPacketTranslator;

public class PacketTranslatorManager {

	private ArrayList<PacketTranslator<?>> packetTranslators = new ArrayList<PacketTranslator<?>>();
	private HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByClass = new HashMap<Class<?>, PacketTranslator<?>>();
	private HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByPacketClass = new HashMap<Class<?>, PacketTranslator<?>>();

	private CopyOnWriteArrayList<IdleThing> idlePackets = new CopyOnWriteArrayList<IdleThing>();//I didn't actually check if I need CopyOnWriteArrayList, but I assume so

	public PacketTranslatorManager() {
		this.addTranslator(new StartGamePacketTranslator());
		this.addTranslator(new ChunkRadiusUpdatedPacketTranslator());
		this.addTranslator(new LevelChunkPacketTranslator());
		this.addTranslator(new PlayStatusPacketTranslator());
		this.addTranslator(new ResourcePacksInfoPacketTranslator());
		this.addTranslator(new ResourcePackStackPacketTranslator());
		this.addTranslator(new AddPlayerPacketTranslator());
		this.addTranslator(new PlayerListPacketTranslator());
		this.addTranslator(new TextPacketTranslator());
		this.addTranslator(new AddEntityPacketTranslator());
		this.addTranslator(new SetTimePacketTranslator());
		this.addTranslator(new RemoveEntityPacketTranslator());
		this.addTranslator(new InventorySlotPacketTranslator());
		this.addTranslator(new AddItemEntityPacketTranslator());
		this.addTranslator(new MovePlayerPacketTranslator());
		this.addTranslator(new MoveEntityAbsolutePacketTranslator());
		this.addTranslator(new ServerToClientHandshakePacketTranslator());
		this.addTranslator(new UpdateBlockPacketTranslator());
		this.addTranslator(new SetEntityMotionPacketTranslator());
		this.addTranslator(new TakeItemEntityPacketTranslator());
		this.addTranslator(new NetworkChunkPublisherUpdatePacketTranslator());
		this.addTranslator(new SetEntityDataPacketTranslator());
		this.addTranslator(new ContainerOpenPacketTranslator());
		this.addTranslator(new InventoryContentPacketTranslator());
		
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

	private class IdleThing {
		private PacketTranslator<BedrockPacket> packetTranslator;
		private BedrockPacket bedrockPacket;

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
