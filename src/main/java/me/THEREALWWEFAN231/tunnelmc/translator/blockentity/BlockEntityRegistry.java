package me.THEREALWWEFAN231.tunnelmc.translator.blockentity;

import com.nukkitx.nbt.NbtMap;

import java.util.HashMap;
import java.util.Map;

public class BlockEntityRegistry {
    private static final Map<String, BlockEntityTranslator> BLOCK_ENTITY_TRANSLATORS = new HashMap<>();

    static {
        BLOCK_ENTITY_TRANSLATORS.put("Sign", new SignBlockEntityTranslator());
    }

    public static BlockEntityTranslator getBlockEntityTranslator(NbtMap bedrockNbt) {
        return BLOCK_ENTITY_TRANSLATORS.get(bedrockNbt.getString("id"));
    }

    public static void load() {
        // no-op
    }
}
