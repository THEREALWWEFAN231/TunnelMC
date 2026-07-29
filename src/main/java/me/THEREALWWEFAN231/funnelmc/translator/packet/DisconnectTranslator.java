package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;

public class DisconnectTranslator extends PacketTranslator<DisconnectPacket> {
    @Override
    public void translate(DisconnectPacket packet) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().disconnectFromWorld(Component.literal("Use Translated Here"));
        }
        Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().disconnect(new DisconnectedScreen(Minecraft.getInstance().gui.screen(), Component.literal("Disconnected"), Component.literal(packet.getKickMessage())), false));
    }

    @Override
    public Class<?> getPacketClass() {
        return DisconnectPacket.class;
    }
}
