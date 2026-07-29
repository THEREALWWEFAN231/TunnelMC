package me.THEREALWWEFAN231.funnelmc.translator.packet.world;

import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.BlockPaletteTranslator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;

public class UpdateBlockTranslator extends PacketTranslator<UpdateBlockPacket> {

	//TODO: probably want to check out flags
	
	@Override
	public void translate(UpdateBlockPacket packet) {
		if (packet.getDefinition() == null) {
			// Definition can be null if the block runtime ID wasn't in our block palette (or the
			// packet decoded against a codec helper that wasn't fully populated yet) - nothing
			// sane to update the block to in that case.
			return;
		}

		BlockPos blockPos = new BlockPos(packet.getBlockPosition().getX(), packet.getBlockPosition().getY(), packet.getBlockPosition().getZ());
		if (packet.getDataLayer() == 0) {
			BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getDefinition().getRuntimeId());

			ClientboundBlockUpdatePacket blockUpdateS2CPacket = new ClientboundBlockUpdatePacket(blockPos, blockState);
			Client.instance.javaConnection.processServerToClientPacket(blockUpdateS2CPacket);

		} else if (packet.getDataLayer() == 1) {
			// Set waterlogged state of existing block
			BlockState blockState = Minecraft.getInstance().level.getBlockState(blockPos);
			BlockState newBlockState;
			if (blockState.isAir()) {
				newBlockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getDefinition().getRuntimeId());
				if (blockState.isAir()) {
					return;
				}
			} else {
				if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
					newBlockState = blockState.setValue(BlockStateProperties.WATERLOGGED, packet.getDefinition().getRuntimeId() == BlockPaletteTranslator.WATER_BEDROCK_BLOCK_ID);
				} else {
					return;
				}
			}

			ClientboundBlockUpdatePacket blockUpdateS2CPacket = new ClientboundBlockUpdatePacket(blockPos, newBlockState);
			Client.instance.javaConnection.processServerToClientPacket(blockUpdateS2CPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return UpdateBlockPacket.class;
	}

}
