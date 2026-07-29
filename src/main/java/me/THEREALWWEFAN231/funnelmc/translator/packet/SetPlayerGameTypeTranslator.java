package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.world.level.GameType;

public class SetPlayerGameTypeTranslator extends PacketTranslator<SetPlayerGameTypePacket> {
    @Override
    public void translate(SetPlayerGameTypePacket packet) {
        GameType javaGameMode;
        switch (packet.getGamemode()) {
            case 0:
                javaGameMode = GameType.SURVIVAL;
                break;
            case 1:
                javaGameMode = GameType.CREATIVE;
                break;
            case 2:
                javaGameMode = GameType.ADVENTURE;
                break;
            default:
                System.out.println("Don't know how to process " + packet.toString());
                return;
        }

        Client.instance.javaConnection.processServerToClientPacket(new ClientboundGameEventPacket(
                ClientboundGameEventPacket.CHANGE_GAME_MODE,
                (float) javaGameMode.getId()));
    }

    @Override
    public Class<?> getPacketClass() {
        return SetPlayerGameTypePacket.class;
    }
}
