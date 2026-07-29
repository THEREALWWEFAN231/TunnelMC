package me.THEREALWWEFAN231.funnelmc.translator.packet.world;

import java.lang.reflect.Constructor;

import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.blockentity.BlockEntityRegistry;
import me.THEREALWWEFAN231.funnelmc.translator.blockentity.BlockEntityTranslator;
import me.THEREALWWEFAN231.funnelmc.utils.PositionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityDataTranslator extends PacketTranslator<BlockEntityDataPacket> {

    // TODO: the old legacy wire-format javaId (getJavaId()) doesn't map onto a modern
    // BlockEntityType registry entry (that's now a proper Registry<BlockEntityType<?>> lookup
    // keyed by identifier, not the old fixed 1.13-era numeric ids). Using reflection to construct
    // the packet with a null type as a placeholder since ClientboundBlockEntityDataPacket's
    // (BlockPos, BlockEntityType, CompoundTag) constructor is private - the public API only
    // offers create(BlockEntity, ...) which needs a real block entity instance we don't have.
    private static final Constructor<ClientboundBlockEntityDataPacket> CONSTRUCTOR;
    static {
        try {
            CONSTRUCTOR = ClientboundBlockEntityDataPacket.class.getDeclaredConstructor(BlockPos.class, BlockEntityType.class, CompoundTag.class);
            CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void translate(BlockEntityDataPacket packet) {
        BlockEntityTranslator translator = BlockEntityRegistry.getBlockEntityTranslator(packet.getData());
        if (translator != null) {
            CompoundTag tag = translator.translateTag(packet.getData());
            try {
                ClientboundBlockEntityDataPacket updatePacket = CONSTRUCTOR.newInstance(
                        PositionUtil.toBlockPos(packet.getBlockPosition()), null, tag);
                Client.instance.javaConnection.processServerToClientPacket(updatePacket);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return BlockEntityDataPacket.class;
    }
}
