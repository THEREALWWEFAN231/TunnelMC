package me.THEREALWWEFAN231.tunnelmc.translator.blockstate;

import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtList;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import com.nukkitx.nbt.util.stream.LittleEndianDataInputStream;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.THEREALWWEFAN231.tunnelmc.utils.FileManagement;
import net.minecraft.block.BlockState;

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
        try (InputStream stream = FileManagement.class.getClassLoader().getResourceAsStream("tunnelmc/runtime_block_states.dat")) {
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
