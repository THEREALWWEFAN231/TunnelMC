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
        if (MinecraftClient.getInstance().world == null) {
            return;
        }

        for (GameRuleData<?> gamerule : gamerules) {
            switch (gamerule.getName()) {
                case "dodaylightcycle":
                    MinecraftClient.getInstance().world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set((boolean) gamerule.getValue(), null);
                    break;
                case "doimmediaterespawn":
                    MinecraftClient.getInstance().player.setShowsDeathScreen(!(boolean) gamerule.getValue());
                    break;
            }
        }
    }
}
