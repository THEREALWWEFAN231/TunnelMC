package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.nukkitx.protocol.bedrock.packet.LevelSoundEventPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.tunnelmc.utils.PositionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class LevelSoundEventTranslator extends PacketTranslator<LevelSoundEventPacket> {
    @Override
    public void translate(LevelSoundEventPacket packet) {
        switch (packet.getSound()) {
            case HIT:
                BlockPos pos = PositionUtil.toBlockPos(packet.getPosition());
                BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getExtraData());
                BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
                MinecraftClient.getInstance().getSoundManager().play(
                        new PositionedSoundInstance(blockSoundGroup.getHitSound(), SoundCategory.BLOCKS,
                                (blockSoundGroup.getVolume() + 1.0F) / 8.0F, blockSoundGroup.getPitch() * 0.5F, pos));
                break;
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return LevelSoundEventPacket.class;
    }
}
