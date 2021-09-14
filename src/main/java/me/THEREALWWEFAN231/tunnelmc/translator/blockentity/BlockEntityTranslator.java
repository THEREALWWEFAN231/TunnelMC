package me.THEREALWWEFAN231.tunnelmc.translator.blockentity;

import com.nukkitx.nbt.NbtMap;
import net.minecraft.nbt.NbtCompound;

public abstract class BlockEntityTranslator {
    public NbtCompound translateTag(NbtMap bedrockNbt) {
        return translateTag(bedrockNbt, getBaseCompoundTag(bedrockNbt));
    }

    public abstract NbtCompound translateTag(NbtMap bedrockNbt, NbtCompound newTag);

    public abstract int getJavaId();

    protected NbtCompound getBaseCompoundTag(NbtMap bedrockNbt) {
        NbtCompound tag = new NbtCompound();
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
