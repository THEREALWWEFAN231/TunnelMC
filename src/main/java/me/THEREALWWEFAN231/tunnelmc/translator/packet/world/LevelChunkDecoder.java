package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtMapBuilder;
import com.nukkitx.nbt.util.stream.NetworkDataInputStream;
import com.nukkitx.network.VarInts;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import me.THEREALWWEFAN231.tunnelmc.nukkit.BitArray;
import me.THEREALWWEFAN231.tunnelmc.nukkit.BitArrayVersion;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.LegacyBlockPaletteManager;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;

import java.io.IOException;

/**
 * The LevelChunkDecoder class contains methods on decoding every existing Bedrock sub chunk format.
 */
public class LevelChunkDecoder {

    /**
     * Version eight is the newest chunk format introduced by Bedrock.
     * It allows up to 256 layers for one sub chunk.
     */
    public static void networkDecodeVersionEight(ByteBuf byteBuf, ChunkSection[] chunkSections, int sectionIndex, byte storageSize) {
        for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
            byte paletteHeader = byteBuf.readByte();
            boolean isRuntime = (paletteHeader & 1) == 1;
            int paletteVersion = (paletteHeader | 1) >> 1;

            BitArrayVersion bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

            int maxBlocksInSection = 4096;
            BitArray bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
            int wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);

            for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
                int word = byteBuf.readIntLE();
                bitArray.getWords()[wordIterationIndex] = word;
            }

            int paletteSize = VarInts.readInt(byteBuf);
            int[] sectionPalette = new int[paletteSize];
            NBTInputStream nbtStream = isRuntime ? null : new NBTInputStream(new NetworkDataInputStream(new ByteBufInputStream(byteBuf)));
            for (int i = 0; i < paletteSize; i++) {
                if (isRuntime) {
                    sectionPalette[i] = VarInts.readInt(byteBuf);
                } else {
                    try {
                        NbtMapBuilder map = ((NbtMap) nbtStream.readTag()).toBuilder();
                        map.replace("name", "minecraft:" + map.get("name").toString());

                        sectionPalette[i] = BlockPaletteTranslator.getBedrockBlockId(BlockPaletteTranslator.bedrockStateFromNBTMap(map.build()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (storageReadIndex == 0) {
                int index = 0;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 16; y++) {
                            int paletteIndex = bitArray.get(index);
                            int mcbeBlockId = sectionPalette[paletteIndex];
                            if (mcbeBlockId != BlockPaletteTranslator.AIR_BEDROCK_BLOCK_ID) {

                                BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(mcbeBlockId);

                                chunkSections[sectionIndex].setBlockState(x, y, z, blockState);
                            }
                            index++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Version one is the second newest chunk format by Bedrock.
     * It is essentially the same as version eight, however there is only 1 layer per subchunk.
     */
    public static void networkDecodeVersionOne(ByteBuf byteBuf, ChunkSection chunkSection) {
        networkDecodeVersionEight(byteBuf, new ChunkSection[]{chunkSection}, 0, (byte) 1);
    }

    /**
     * Version zero is a chunk format used on PocketMine (3.0.0) and Nukkit (1.0) servers.
     * It is a legacy format that most third party servers use.
     */
    public static void networkDecodeVersionZero(ByteBuf byteBuf, ChunkSection chunkSection) {
        byte[] blockIds = new byte[4096];
        byteBuf.readBytes(blockIds);

        byte[] metaIds = new byte[2048];
        byteBuf.readBytes(metaIds);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 8) + (z << 4) + y;

                    int id = blockIds[index];
                    int meta = metaIds[index >> 1] >> (index & 1) * 4 & 15;

                    BlockState blockState = LegacyBlockPaletteManager.LEGACY_BLOCK_TO_JAVA_ID.get(id << 6 | meta);

                    if (blockState != null) {
                        chunkSection.setBlockState(x, y, z, blockState);
                    }
                }
            }
        }
    }

}