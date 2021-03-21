package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.protocol.bedrock.packet.SetPlayerGameTypePacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.world.GameMode;

public class SetPlayerGameTypeTranslator extends PacketTranslator<SetPlayerGameTypePacket> {
    @Override
    public void translate(SetPlayerGameTypePacket packet) {
        GameMode javaGameMode;
        switch (packet.getGamemode()) {
            case 0:
                javaGameMode = GameMode.SURVIVAL;
                break;
            case 1:
                javaGameMode = GameMode.CREATIVE;
                break;
            case 2:
                javaGameMode = GameMode.ADVENTURE;
                break;
            default:
                System.out.println("Don't know how to process " + packet.toString());
                return;
        }

        Client.instance.javaConnection.processServerToClientPacket(new GameStateChangeS2CPacket(
                GameStateChangeS2CPacket.GAME_MODE_CHANGED,
                (float) javaGameMode.getId()));
    }

    @Override
    public Class<?> getPacketClass() {
        return SetPlayerGameTypePacket.class;
    }
}
