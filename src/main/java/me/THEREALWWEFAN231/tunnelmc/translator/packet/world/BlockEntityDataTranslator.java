package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.nukkitx.protocol.bedrock.packet.BlockEntityDataPacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockentity.BlockEntityRegistry;
import me.THEREALWWEFAN231.tunnelmc.translator.blockentity.BlockEntityTranslator;
import me.THEREALWWEFAN231.tunnelmc.utils.PositionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

public class BlockEntityDataTranslator extends PacketTranslator<BlockEntityDataPacket> {

    @Override
    public void translate(BlockEntityDataPacket packet) {
        BlockEntityTranslator translator = BlockEntityRegistry.getBlockEntityTranslator(packet.getData());
        if (translator != null) {
            CompoundTag tag = translator.translateTag(packet.getData());
            BlockEntityUpdateS2CPacket updatePacket = new BlockEntityUpdateS2CPacket(
                    PositionUtil.toBlockPos(packet.getBlockPosition()), translator.getJavaId(), tag);
            Client.instance.javaConnection.processServerToClientPacket(updatePacket);
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return BlockEntityDataPacket.class;
    }

}