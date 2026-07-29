package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;

public class BlockEntityDataPacketTranslator extends PacketTranslator<BlockEntityDataPacket> {

	@Override
	public void translate(BlockEntityDataPacket packet) {
		
		Vector3i blockPosition = packet.getBlockPosition();
		NbtMap blockEntityData = packet.getData();
		
		Client.instance.blockEntityDataCache.getCachedBlockPositionsData().put(blockPosition, blockEntityData);
	}

	@Override
	public Class<?> getPacketClass() {
		return BlockEntityDataPacket.class;
	}

}
