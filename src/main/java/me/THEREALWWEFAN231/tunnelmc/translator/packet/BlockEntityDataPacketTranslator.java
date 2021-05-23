package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.protocol.bedrock.packet.BlockEntityDataPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

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
