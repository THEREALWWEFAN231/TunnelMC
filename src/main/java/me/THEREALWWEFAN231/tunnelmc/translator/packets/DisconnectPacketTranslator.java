package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.DisconnectPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.text.Text;

public class DisconnectPacketTranslator extends PacketTranslator<DisconnectPacket> {
    @Override
    public void translate(DisconnectPacket packet) {
        if (MinecraftClient.getInstance().world != null) {
            MinecraftClient.getInstance().world.disconnect();
        }
        MinecraftClient.getInstance().disconnect(new DisconnectedScreen(MinecraftClient.getInstance().currentScreen, Text.of("Use Translated Here"), Text.of(packet.getKickMessage())));
    }

    @Override
    public Class<?> getPacketClass() {
        return DisconnectPacket.class;
    }
}
