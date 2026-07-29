package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.packet.GameRulesChangedPacket;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.client.Minecraft;

import java.util.List;

public class GameRulesChangedTranslator extends PacketTranslator<GameRulesChangedPacket> {
    @Override
    public void translate(GameRulesChangedPacket packet) {
        if (Minecraft.getInstance().level == null) {
            Minecraft.getInstance().execute(() -> onGameRulesChanged(packet.getGameRules()));
        } else {
            onGameRulesChanged(packet.getGameRules());
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return GameRulesChangedPacket.class;
    }

    public static void onGameRulesChanged(List<GameRuleData<?>> gamerules) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        for (GameRuleData<?> gamerule : gamerules) {
            switch (gamerule.getName()) {
                case "dodaylightcycle":
                    // TODO: ClientLevel no longer exposes a GameRules accessor the way it did in
                    // 1.16.5 - need to find the modern equivalent (likely via ClientLevelData) to
                    // mirror this rule.
                    break;
                case "doimmediaterespawn":
                    Minecraft.getInstance().player.setShowDeathScreen(!((Boolean) gamerule.getValue()));
                    break;
            }
        }
    }
}
