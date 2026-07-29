package me.THEREALWWEFAN231.funnelmc.translator.blockstate;

import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtList;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.util.stream.LittleEndianDataInputStream;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.THEREALWWEFAN231.funnelmc.utils.FileManagement;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Used for server softwares that use an older chunk encoding version that use a different block palette.
 */
public final class LegacyBlockPaletteManager {
    public static final Int2ObjectMap<BlockState> LEGACY_BLOCK_TO_JAVA_ID = new Int2ObjectOpenHashMap<>();

    static {
        NbtList<NbtMap> legacyBlockStates;
        try (InputStream stream = FileManagement.class.getClassLoader().getResourceAsStream("funnelmc/runtime_block_states.dat")) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block state tag!");
            }
            try (NBTInputStream nbtStream = new NBTInputStream(new LittleEndianDataInputStream(stream))) {
                //noinspection unchecked
                legacyBlockStates = (NbtList<NbtMap>) nbtStream.readTag();
            }
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette", e);
        }

        int bedrockRuntimeId = -1;
        for (NbtMap nbt : legacyBlockStates) {
            bedrockRuntimeId++;
            List<NbtMap> legacyStates = nbt.getList("LegacyStates", NbtType.COMPOUND);
            if (legacyStates == null) {
                continue;
            }

            for (NbtMap legacyState : legacyStates) {
                int legacyId = legacyState.getInt("id") << 6 | legacyState.getShort("val");
                LEGACY_BLOCK_TO_JAVA_ID.put(legacyId, BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(bedrockRuntimeId));
            }
        }
    }
}
