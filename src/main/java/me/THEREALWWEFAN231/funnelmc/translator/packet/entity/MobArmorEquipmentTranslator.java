package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import com.mojang.datafixers.util.Pair;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.MobArmorEquipmentPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.item.ItemTranslator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;

import java.util.List;

public class MobArmorEquipmentTranslator extends PacketTranslator<MobArmorEquipmentPacket> {
    @Override
    public void translate(MobArmorEquipmentPacket packet) {
        List<Pair<EquipmentSlot, ItemStack>> javaArmorSlots = new ObjectArrayList<>(4);
        javaArmorSlots.add(translateArmorSlot(packet.getHelmet(), EquipmentSlot.HEAD));
        javaArmorSlots.add(translateArmorSlot(packet.getChestplate(), EquipmentSlot.CHEST));
        javaArmorSlots.add(translateArmorSlot(packet.getLeggings(), EquipmentSlot.LEGS));
        javaArmorSlots.add(translateArmorSlot(packet.getBoots(), EquipmentSlot.FEET));

        ClientboundSetEquipmentPacket equipmentUpdatePacket = new ClientboundSetEquipmentPacket((int) packet.getRuntimeEntityId(),
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
