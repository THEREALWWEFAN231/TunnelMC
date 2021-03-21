package me.THEREALWWEFAN231.tunnelmc.translator.blockentity;

import com.nukkitx.nbt.NbtMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

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

        // TODO use Adventure
        newTag.putString("Text1", Text.Serializer.toJson(new LiteralText(javaText[0])));
        newTag.putString("Text2", Text.Serializer.toJson(new LiteralText(javaText[1])));
        newTag.putString("Text3", Text.Serializer.toJson(new LiteralText(javaText[2])));
        newTag.putString("Text4", Text.Serializer.toJson(new LiteralText(javaText[3])));
        return newTag;
    }

    @Override
    public int getJavaId() {
        return 9;
    }
}
