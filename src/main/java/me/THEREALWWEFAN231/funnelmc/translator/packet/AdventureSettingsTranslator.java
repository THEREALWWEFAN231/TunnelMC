package me.THEREALWWEFAN231.funnelmc.translator.packet;

import org.cloudburstmc.protocol.bedrock.data.AdventureSetting;
import org.cloudburstmc.protocol.bedrock.packet.AdventureSettingsPacket;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;

public class AdventureSettingsTranslator extends PacketTranslator<AdventureSettingsPacket> {
    @Override
    public void translate(AdventureSettingsPacket packet) {
        Abilities abilities = new Abilities();
        abilities.mayfly = packet.getSettings().contains(AdventureSetting.MAY_FLY);
        abilities.mayBuild = packet.getSettings().contains(AdventureSetting.BUILD);
        abilities.flying = packet.getSettings().contains(AdventureSetting.FLYING);
        abilities.invulnerable = false;

        ClientboundPlayerAbilitiesPacket abilitiesPacket = new ClientboundPlayerAbilitiesPacket(abilities);
        Client.instance.javaConnection.processServerToClientPacket(abilitiesPacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return AdventureSettingsPacket.class;
    }
}
