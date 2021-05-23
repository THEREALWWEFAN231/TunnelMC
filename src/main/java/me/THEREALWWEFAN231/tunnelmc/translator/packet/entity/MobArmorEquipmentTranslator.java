package me.THEREALWWEFAN231.tunnelmc.translator.packet.entity;

import com.mojang.datafixers.util.Pair;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.packet.MobArmorEquipmentPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;

import java.util.List;

public class MobArmorEquipmentTranslator extends PacketTranslator<MobArmorEquipmentPacket> {

    @Override
    public void translate(MobArmorEquipmentPacket packet) {
        List<Pair<EquipmentSlot, ItemStack>> javaArmorSlots = new ObjectArrayList<>(4);
        javaArmorSlots.add(translateArmorSlot(packet.getHelmet(), EquipmentSlot.HEAD));
        javaArmorSlots.add(translateArmorSlot(packet.getChestplate(), EquipmentSlot.CHEST));
        javaArmorSlots.add(translateArmorSlot(packet.getLeggings(), EquipmentSlot.LEGS));
        javaArmorSlots.add(translateArmorSlot(packet.getBoots(), EquipmentSlot.FEET));

        EntityEquipmentUpdateS2CPacket equipmentUpdatePacket = new EntityEquipmentUpdateS2CPacket((int) packet.getRuntimeEntityId(),
                javaArmorSlots);
        Client.instance.javaConnection.processServerToClientPacket(equipmentUpdatePacket);
    }

    @Override
    public Class<?> getPacketClass() {
        return MobArmorEquipmentPacket.class;
    }

    private Pair<EquipmentSlot, ItemStack> translateArmorSlot(ItemData bedrockItem, EquipmentSlot slot) {
        return new Pair<>(slot, ItemTranslator.itemDataToItemStack(bedrockItem));
    }

}