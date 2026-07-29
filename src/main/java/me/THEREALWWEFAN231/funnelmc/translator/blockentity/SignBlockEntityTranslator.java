package me.THEREALWWEFAN231.funnelmc.translator.blockentity;

import org.cloudburstmc.nbt.NbtMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import com.mojang.serialization.JsonOps;

public class SignBlockEntityTranslator extends BlockEntityTranslator {
    @Override
    public CompoundTag translateTag(NbtMap bedrockNbt, CompoundTag newTag) {
        String text = bedrockNbt.getString("Text");
        int textCount = 0;
        String[] javaText = {"", "", "", ""};

        //TODO: Improve this - I want to figure out if we can use Minecraft internals before using Geyser's sign wrapping implementation
        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                javaText[textCount] = builder.toString();
                textCount++;
                if (textCount > 3) {
                    break;
                }
                builder = new StringBuilder();
                continue;
            }
            builder.append(c);
        }

        // TODO use Adventure. Also note modern sign block entities store their text under
        // nested front_text/back_text compounds (with a "messages" list + glowing/color/dyed
        // fields), not flat Text1..Text4 keys - this still writes the old 1.16.5 layout.
        // TODO use Adventure
        newTag.putString("Text1", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, Component.literal(javaText[0])).getOrThrow().toString());
        newTag.putString("Text2", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, Component.literal(javaText[1])).getOrThrow().toString());
        newTag.putString("Text3", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, Component.literal(javaText[2])).getOrThrow().toString());
        newTag.putString("Text4", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, Component.literal(javaText[3])).getOrThrow().toString());
        return newTag;
    }

    @Override
    public int getJavaId() {
        return 9;
    }
}
