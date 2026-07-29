package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import com.mojang.datafixers.util.Pair;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId;
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.item.ItemTranslator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;

import java.util.Collections;

/**
 * Sent when an entity changes their main hand or offhand
 */
public class MobEquipmentTranslator extends PacketTranslator<MobEquipmentPacket> {
    @Override
    public void translate(MobEquipmentPacket packet) {
        EquipmentSlot equipmentSlot = null;
        switch (packet.getContainerId()) {
            case ContainerId.INVENTORY:
                equipmentSlot = EquipmentSlot.MAINHAND;
                break;
            case ContainerId.OFFHAND:
                equipmentSlot = EquipmentSlot.OFFHAND;
                break;
        }

        if (equipmentSlot != null) {
            Pair<EquipmentSlot, ItemStack> itemStackPair = new Pair<>(equipmentSlot, ItemTranslator.itemDataToItemStack(packet.getItem()));
            ClientboundSetEquipmentPacket equipmentUpdatePacket = new ClientboundSetEquipmentPacket((int) packet.getRuntimeEntityId(),
                    Collections.singletonList(itemStackPair));
            Client.instance.javaConnection.processServerToClientPacket(equipmentUpdatePacket);
        } else {
            System.out.println("Not sure how to handle MobEquipmentPacket: " + packet.toString());
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return MobEquipmentPacket.class;
    }
}
