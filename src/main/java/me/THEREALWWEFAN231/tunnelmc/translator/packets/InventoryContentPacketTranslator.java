package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import com.nukkitx.protocol.bedrock.packet.InventoryContentPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.ServerInventoryCache;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;

public class InventoryContentPacketTranslator extends PacketTranslator<InventoryContentPacket> {

	@Override
	public void translate(InventoryContentPacket packet) {

		int syncId = packet.getContainerId();
		DefaultedList<ItemStack> contents = DefaultedList.ofSize(packet.getContents().size(), ItemStack.EMPTY);
		for (int i = 0; i < packet.getContents().size(); i++) {
			ItemStack itemStack = ItemTranslator.itemDataToItemStack(packet.getContents().get(i));

			contents.set(i, itemStack);
			ServerInventoryCache.putItemInInventory(packet.getContainerId(), i, packet.getContents().get(i));
		}

		InventoryS2CPacket inventoryS2CPacket = new InventoryS2CPacket(syncId, contents);
		Client.instance.javaConnection.processServerToClientPacket(inventoryS2CPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return InventoryContentPacket.class;
	}

}