package me.THEREALWWEFAN231.funnelmc.translator.packet.world;

import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.funnelmc.utils.PositionUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class LevelSoundEventTranslator extends PacketTranslator<LevelSoundEventPacket> {
    @Override
    public void translate(LevelSoundEventPacket packet) {
        switch (packet.getSound()) {
            case HIT:
                BlockPos pos = PositionUtil.toBlockPos(packet.getPosition());
                BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getExtraData());
                SoundType soundType = blockState.getSoundType();
                Minecraft.getInstance().getSoundManager().play(
                        new SimpleSoundInstance(soundType.getHitSound(), SoundSource.BLOCKS,
                                (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F, RandomSource.create(), pos));
                break;
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return LevelSoundEventPacket.class;
    }
}
