package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.protocol.bedrock.packet.DisconnectPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

public class DisconnectTranslator extends PacketTranslator<DisconnectPacket> {
    @Override
    public void translate(DisconnectPacket packet) {
        if (MinecraftClient.getInstance().world != null) {
            MinecraftClient.getInstance().world.disconnect();
        }
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
                new DisconnectedScreen(
                        new TitleScreen(false),
                        Text.of("TunnelMC"),
                        Text.of(packet.getKickMessage())
                )
        ));
    }

    @Override
    public Class<?> getPacketClass() {
        return DisconnectPacket.class;
    }
}
