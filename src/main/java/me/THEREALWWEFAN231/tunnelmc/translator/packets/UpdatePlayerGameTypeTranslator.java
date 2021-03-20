package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.UpdatePlayerGameTypePacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.gamemode.GameModeTranslator;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

public class UpdatePlayerGameTypeTranslator extends PacketTranslator<UpdatePlayerGameTypePacket> {
    @Override
    public void translate(UpdatePlayerGameTypePacket packet) {
        // Untested
        Client.instance.javaConnection.processServerToClientPacket(new GameStateChangeS2CPacket(
                GameStateChangeS2CPacket.GAME_MODE_CHANGED,
                (float) GameModeTranslator.bedrockToJava(packet.getGameType(), StartGameTranslator.DEFAULT_GAME_TYPE).getId()));
    }

    @Override
    public Class<?> getPacketClass() {
        return UpdatePlayerGameTypePacket.class;
    }
}
