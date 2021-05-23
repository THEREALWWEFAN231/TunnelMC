package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.protocol.bedrock.data.GameRuleData;
import com.nukkitx.protocol.bedrock.packet.GameRulesChangedPacket;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.GameRules;

import java.util.List;

public class GameRulesChangedTranslator extends PacketTranslator<GameRulesChangedPacket> {

    @Override
    public void translate(GameRulesChangedPacket packet) {
        if (MinecraftClient.getInstance().world == null) {
            MinecraftClient.getInstance().execute(() -> onGameRulesChanged(packet.getGameRules()));
        } else {
            onGameRulesChanged(packet.getGameRules());
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return GameRulesChangedPacket.class;
    }

    public static void onGameRulesChanged(List<GameRuleData<?>> gamerules) {
        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) {
            return;
        }

        for (GameRuleData<?> gameRule : gamerules) {
            switch (gameRule.getName()) {
                case "dodaylightcycle": {
                    MinecraftClient.getInstance().world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(((Boolean) gameRule.getValue()), null);
                    break;
                }
                case "doimmediaterespawn": {
                    MinecraftClient.getInstance().player.setShowsDeathScreen(!((Boolean) gameRule.getValue()));
                    break;
                }
            }
        }
    }

}