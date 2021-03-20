package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.data.AdventureSetting;
import com.nukkitx.protocol.bedrock.packet.AdventureSettingsPacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;

public class AdventureSettingsTranslator extends PacketTranslator<AdventureSettingsPacket> {
    @Override
    public void translate(AdventureSettingsPacket packet) {
        PlayerAbilities abilities = new PlayerAbilities();
        abilities.allowFlying = packet.getSettings().contains(AdventureSetting.MAY_FLY);
        abilities.allowModifyWorld = packet.getSettings().contains(AdventureSetting.BUILD);
        abilities.flying = packet.getSettings().contains(AdventureSetting.FLYING);
        abilities.invulnerable = false;

        PlayerAbilitiesS2CPacket abilitiesPacket = new PlayerAbilitiesS2CPacket(abilities);
        Client.instance.javaConnection.processServerToClientPacket(abilitiesPacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return AdventureSettingsPacket.class;
    }
}
