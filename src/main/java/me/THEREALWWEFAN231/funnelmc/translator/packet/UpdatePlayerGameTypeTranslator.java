package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.protocol.bedrock.packet.UpdatePlayerGameTypePacket;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.gamemode.GameModeTranslator;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.world.level.GameType;

public class UpdatePlayerGameTypeTranslator extends PacketTranslator<UpdatePlayerGameTypePacket> {
    @Override
    public void translate(UpdatePlayerGameTypePacket packet) {
        GameType javaGameMode = GameModeTranslator.bedrockToJava(packet.getGameType(), StartGameTranslator.DEFAULT_GAME_TYPE);

        Client.instance.javaConnection.processServerToClientPacket(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.CHANGE_GAME_MODE,
                (float) javaGameMode.getId()));
    }

    @Override
    public Class<?> getPacketClass() {
        return UpdatePlayerGameTypePacket.class;
    }
}
