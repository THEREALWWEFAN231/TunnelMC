package me.THEREALWWEFAN231.tunnelmc.translator.packet.inventory;

import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.packet.InventoryContentPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainers;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler.ScreenHandlerTranslatorManager;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;

public class InventoryContentPacketTranslator extends PacketTranslator<InventoryContentPacket> {

	@Override
	public void translate(InventoryContentPacket packet) {

		int syncId = packet.getContainerId();
		int javaContainerSize = packet.getContents().size();

		BedrockContainer containerAffected = Client.instance.containers.getContainers().get(syncId);//player container, armor container, etc
		if (containerAffected == null) {
			containerAffected = Client.instance.containers.getCurrentlyOpenContainer();
		}

		switch (syncId) {
		case BedrockContainers.PLAYER_INVENTORY_COTNAINER_ID:

			for (int i = 0; i < javaContainerSize; i++) {
				ItemData bedrockItemStack = packet.getContents().get(i);
				ItemStack translatedStack = ItemTranslator.itemDataToItemStack(bedrockItemStack);

				int javaSlotId = ScreenHandlerTranslatorManager.getJavaSlotFromBedrockContainer(TunnelMC.mc.player.currentScreenHandler, containerAffected, i);

				containerAffected.setItemBedrock(i, bedrockItemStack);
				TunnelMC.mc.player.playerScreenHandler.getSlot(javaSlotId).setStack(translatedStack);
			}

			break;

		case BedrockContainers.PLAYER_ARMOR_COTNAINER_ID:
			for (int i = 0; i < javaContainerSize; i++) {
				ItemData bedrockItemStack = packet.getContents().get(i);
				ItemStack translatedStack = ItemTranslator.itemDataToItemStack(bedrockItemStack);

				containerAffected.setItemBedrock(i, bedrockItemStack);
				TunnelMC.mc.player.playerScreenHandler.getSlot(5 + i).setStack(translatedStack);
			}
			break;

		case BedrockContainers.PLAYER_OFFHAND_COTNAINER_ID: {
			ItemData bedrockItemStack = packet.getContents().get(0);
			ItemStack translatedStack = ItemTranslator.itemDataToItemStack(bedrockItemStack);

			containerAffected.setItemBedrock(0, bedrockItemStack);
			TunnelMC.mc.player.playerScreenHandler.getSlot(45).setStack(translatedStack);
			break;
		}
		default://basically TODO: currently works when opening a single chest but yeah..

			DefaultedList<ItemStack> javaContents = DefaultedList.ofSize(packet.getContents().size(), ItemStack.EMPTY);

			for (int i = 0; i < javaContainerSize; i++) {
				ItemData bedrockItemStack = packet.getContents().get(i);
				ItemStack translatedStack = ItemTranslator.itemDataToItemStack(bedrockItemStack);

				javaContents.set(i, translatedStack);
				containerAffected.setItemBedrock(i, packet.getContents().get(i));
			}

			InventoryS2CPacket inventoryS2CPacket = new InventoryS2CPacket(syncId, javaContents);
			Client.instance.javaConnection.processServerToClientPacket(inventoryS2CPacket);

			break;
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return InventoryContentPacket.class;
	}

}