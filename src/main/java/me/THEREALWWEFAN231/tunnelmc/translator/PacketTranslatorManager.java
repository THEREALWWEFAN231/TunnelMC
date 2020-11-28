package me.THEREALWWEFAN231.tunnelmc.translator;

import java.util.ArrayList;
import java.util.HashMap;

import com.nukkitx.protocol.bedrock.BedrockPacket;

import me.THEREALWWEFAN231.tunnelmc.translator.packets.*;

public class PacketTranslatorManager {

	private ArrayList<PacketTranslator<?>> packetTranslators = new ArrayList<PacketTranslator<?>>();
	private HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByClass = new HashMap<Class<?>, PacketTranslator<?>>();
	private HashMap<Class<?>, PacketTranslator<?>> packetTranslatorsByPacketClass = new HashMap<Class<?>, PacketTranslator<?>>();

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
	}

	private void addTranslator(PacketTranslator<?> translator) {
		this.packetTranslators.add(translator);
		this.packetTranslatorsByClass.put(translator.getClass(), translator);
		this.packetTranslatorsByPacketClass.put(translator.getPacketClass(), translator);
	}

	public void translatePacket(BedrockPacket bedrockPacket) {
		System.out.println(bedrockPacket.toString());
		PacketTranslator packetTranslator = this.packetTranslatorsByPacketClass.get(bedrockPacket.getClass());
		if (packetTranslator != null) {
			packetTranslator.translate(bedrockPacket);
		} else {
			//System.out.println("Could not find a packet translator for the packet: " + bedrockPacket.getClass());
		}
	}

}
