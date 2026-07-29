package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;
import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;

public class AnimateTranslator extends PacketTranslator<AnimatePacket> {
    @Override
    public void translate(AnimatePacket packet) {
        if (FunnelMC.mc.level == null) {
            return;
        }
        Entity entity = FunnelMC.mc.level.getEntity((int) packet.getRuntimeEntityId());
        if (entity == null) {
            return;
        }

        switch (packet.getAction()) {
            case SWING_ARM:
                ClientboundAnimatePacket swingArmPacket = new ClientboundAnimatePacket(entity, 0);
                Client.instance.javaConnection.processServerToClientPacket(swingArmPacket);
                break;
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return AnimatePacket.class;
    }
}
