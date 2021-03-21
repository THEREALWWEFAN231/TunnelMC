package me.THEREALWWEFAN231.tunnelmc.translator.blockentity;

import com.nukkitx.nbt.NbtMap;
import net.minecraft.nbt.CompoundTag;

public abstract class BlockEntityTranslator {
    public CompoundTag translateTag(NbtMap bedrockNbt) {
        return translateTag(bedrockNbt, getBaseCompoundTag(bedrockNbt));
    }

    public abstract CompoundTag translateTag(NbtMap bedrockNbt, CompoundTag newTag);

    public abstract int getJavaId();

    protected CompoundTag getBaseCompoundTag(NbtMap bedrockNbt) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", bedrockNbt.getInt("x"));
        tag.putInt("y", bedrockNbt.getInt("y"));
        tag.putInt("z", bedrockNbt.getInt("z"));
        tag.putString("id", getJavaBlockEntityId(bedrockNbt.getString("id")));
        return tag;
    }

    protected String getJavaBlockEntityId(String bedrockBlockEntityId) {
        switch (bedrockBlockEntityId) {
            // Some specific cases to handle
            // Also note: Chest can be trapped_chest
            case "EnchantTable":
                return "minecraft:enchanting_table";
            case "JigsawBlock":
                return "minecraft:jigsaw";
            case "PistonArm":
                return "minecraft:piston_head";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("minecraft:");
        int index = 0;
        for (char c : bedrockBlockEntityId.toCharArray()) {
            if (Character.isUpperCase(c) && index != 0) {
                builder.append("_").append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
            index++;
        }
        return builder.toString();
    }
}
