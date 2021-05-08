package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.protocol.bedrock.data.AdventureSetting;
import com.nukkitx.protocol.bedrock.data.PlayerPermission;
import com.nukkitx.protocol.bedrock.data.command.CommandPermission;
import com.nukkitx.protocol.bedrock.packet.AdventureSettingsPacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;

public class UpdatePlayerAbilitiesTranslator extends PacketTranslator<UpdatePlayerAbilitiesC2SPacket> {
    @Override
    public void translate(UpdatePlayerAbilitiesC2SPacket packet) {
        AdventureSettingsPacket settingsPacket = new AdventureSettingsPacket();
        if (packet.isFlying()) {
            // Otherwise certain updates can stop the player from flying
            settingsPacket.getSettings().add(AdventureSetting.FLYING);
        }
        settingsPacket.setPlayerPermission(PlayerPermission.MEMBER); // needed?
        settingsPacket.setCommandPermission(CommandPermission.NORMAL); // needed?

        Client.instance.sendPacket(settingsPacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return UpdatePlayerAbilitiesC2SPacket.class;
    }
}
