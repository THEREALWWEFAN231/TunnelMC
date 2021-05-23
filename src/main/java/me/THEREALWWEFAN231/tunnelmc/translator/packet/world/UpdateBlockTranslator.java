package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.nukkitx.protocol.bedrock.packet.UpdateBlockPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class UpdateBlockTranslator extends PacketTranslator<UpdateBlockPacket> {

	//TODO: Probably want to check out flags.
	
	@Override
	public void translate(UpdateBlockPacket packet) {
		BlockPos blockPos = new BlockPos(packet.getBlockPosition().getX(), packet.getBlockPosition().getY(), packet.getBlockPosition().getZ());
		if (packet.getDataLayer() == 0) {
			BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getRuntimeId());

			BlockUpdateS2CPacket blockUpdateS2CPacket = new BlockUpdateS2CPacket(blockPos, blockState);
			Client.instance.javaConnection.processServerToClientPacket(blockUpdateS2CPacket);

		} else if (packet.getDataLayer() == 1) {
			// Set waterlogged state of existing block.
			BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockPos);
			BlockState newBlockState;
			if (blockState.isAir()) {
				newBlockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getRuntimeId());
				if (blockState.isAir()) {
					return;
				}
			} else {
				if (blockState.contains(Properties.WATERLOGGED)) {
					newBlockState = blockState.with(Properties.WATERLOGGED, packet.getRuntimeId() == BlockPaletteTranslator.WATER_BEDROCK_BLOCK_ID);
				} else {
					return;
				}
			}

			BlockUpdateS2CPacket blockUpdateS2CPacket = new BlockUpdateS2CPacket(blockPos, newBlockState);
			Client.instance.javaConnection.processServerToClientPacket(blockUpdateS2CPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return UpdateBlockPacket.class;
	}

}