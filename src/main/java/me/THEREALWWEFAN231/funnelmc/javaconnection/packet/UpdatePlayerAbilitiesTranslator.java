package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import org.cloudburstmc.protocol.bedrock.data.AdventureSetting;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission;
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission;
import org.cloudburstmc.protocol.bedrock.packet.AdventureSettingsPacket;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;

public class UpdatePlayerAbilitiesTranslator extends PacketTranslator<ServerboundPlayerAbilitiesPacket> {
    @Override
    public void translate(ServerboundPlayerAbilitiesPacket packet) {
        AdventureSettingsPacket settingsPacket = new AdventureSettingsPacket();
        if (packet.isFlying()) {
            // Otherwise certain updates can stop the player from flying
            settingsPacket.getSettings().add(AdventureSetting.FLYING);
        }
        settingsPacket.setPlayerPermission(PlayerPermission.MEMBER); // needed?
        settingsPacket.setCommandPermission(CommandPermission.ANY); // needed?

        Client.instance.sendPacket(settingsPacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return ServerboundPlayerAbilitiesPacket.class;
    }
}
