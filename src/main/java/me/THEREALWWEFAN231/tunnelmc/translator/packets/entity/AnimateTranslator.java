package me.THEREALWWEFAN231.tunnelmc.translator.packets.entity;

import com.nukkitx.protocol.bedrock.packet.AnimatePacket;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;

public class AnimateTranslator extends PacketTranslator<AnimatePacket> {
    @Override
    public void translate(AnimatePacket packet) {
        if (TunnelMC.mc.world == null) {
            return;
        }
        Entity entity = TunnelMC.mc.world.getEntityById((int) packet.getRuntimeEntityId());
        if (entity == null) {
            return;
        }

        switch (packet.getAction()) {
            case SWING_ARM:
                EntityAnimationS2CPacket swingArmPacket = new EntityAnimationS2CPacket(entity, 0);
                Client.instance.javaConnection.processServerToClientPacket(swingArmPacket);
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return AnimatePacket.class;
    }
}
