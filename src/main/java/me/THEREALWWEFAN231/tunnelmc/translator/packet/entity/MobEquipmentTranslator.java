package me.THEREALWWEFAN231.tunnelmc.translator.packet.entity;

import com.mojang.datafixers.util.Pair;
import com.nukkitx.protocol.bedrock.data.inventory.ContainerId;
import com.nukkitx.protocol.bedrock.packet.MobEquipmentPacket;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;

import java.util.Collections;

public class MobEquipmentTranslator extends PacketTranslator<MobEquipmentPacket> {

    @Override
    public void translate(MobEquipmentPacket packet) {
        EquipmentSlot equipmentSlot;
        switch (packet.getContainerId()) {
            case ContainerId.INVENTORY:
                equipmentSlot = EquipmentSlot.MAINHAND;
                break;
            case ContainerId.OFFHAND:
                equipmentSlot = EquipmentSlot.OFFHAND;
                break;
            default:
                System.out.println("Not sure how to handle MobEquipmentPacket: " + packet.toString());
                return;
        }

        Pair<EquipmentSlot, ItemStack> itemStackPair = new Pair<>(equipmentSlot, ItemTranslator.itemDataToItemStack(packet.getItem()));
        EntityEquipmentUpdateS2CPacket equipmentUpdatePacket = new EntityEquipmentUpdateS2CPacket((int) packet.getRuntimeEntityId(),
                Collections.singletonList(itemStackPair));
        Client.instance.javaConnection.processServerToClientPacket(equipmentUpdatePacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return MobEquipmentPacket.class;
    }

}