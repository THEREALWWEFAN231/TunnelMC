package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.UpdateBlockPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.ServerBlockPaletteTranslator;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class UpdateBlockPacketTranslator extends PacketTranslator<UpdateBlockPacket> {

	//TODO: probably want to check out flags
	
	@Override
	public void translate(UpdateBlockPacket packet) {

		if (packet.getDataLayer() == 0) {//we cant use data layer 1??!?!?! :thinking:

			BlockPos blockPos = new BlockPos(packet.getBlockPosition().getX(), packet.getBlockPosition().getY(), packet.getBlockPosition().getZ());
			BlockState blockState = ServerBlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getRuntimeId());

			BlockUpdateS2CPacket blockUpdateS2CPacket = new BlockUpdateS2CPacket(blockPos, blockState);
			Client.instance.javaConnection.processServerToClientPacket(blockUpdateS2CPacket);

		}

	}

	@Override
	public Class<?> getPacketClass() {
		return UpdateBlockPacket.class;
	}

}
