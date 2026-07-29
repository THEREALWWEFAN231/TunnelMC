package me.THEREALWWEFAN231.funnelmc.translator.packet.inventory;

import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainers;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.container.screenhandler.ScreenHandlerTranslatorManager;
import me.THEREALWWEFAN231.funnelmc.translator.item.ItemTranslator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.core.NonNullList;

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

				int javaSlotId = ScreenHandlerTranslatorManager.getJavaSlotFromBedrockContainer(FunnelMC.mc.player.containerMenu, containerAffected, i);

				containerAffected.setItemBedrock(i, bedrockItemStack);
				if (javaSlotId != -1) {
					FunnelMC.mc.player.inventoryMenu.getSlot(javaSlotId).set(translatedStack);
				}
			}

			break;

		case BedrockContainers.PLAYER_ARMOR_COTNAINER_ID:
			for (int i = 0; i < javaContainerSize; i++) {
				ItemData bedrockItemStack = packet.getContents().get(i);
				ItemStack translatedStack = ItemTranslator.itemDataToItemStack(bedrockItemStack);

				containerAffected.setItemBedrock(i, bedrockItemStack);
				FunnelMC.mc.player.inventoryMenu.getSlot(5 + i).set(translatedStack);
			}
			break;

		case BedrockContainers.PLAYER_OFFHAND_COTNAINER_ID: {
			ItemData bedrockItemStack = packet.getContents().get(0);
			ItemStack translatedStack = ItemTranslator.itemDataToItemStack(bedrockItemStack);

			containerAffected.setItemBedrock(0, bedrockItemStack);
			FunnelMC.mc.player.inventoryMenu.getSlot(45).set(translatedStack);
			break;
		}
		default://basically TODO: currently works when opening a single chest but yeah..

			NonNullList<ItemStack> javaContents = NonNullList.withSize(packet.getContents().size(), ItemStack.EMPTY);

			for (int i = 0; i < javaContainerSize; i++) {
				ItemData bedrockItemStack = packet.getContents().get(i);
				ItemStack translatedStack = ItemTranslator.itemDataToItemStack(bedrockItemStack);

				javaContents.set(i, translatedStack);
				containerAffected.setItemBedrock(i, packet.getContents().get(i));
			}

			ClientboundContainerSetContentPacket inventoryS2CPacket = new ClientboundContainerSetContentPacket(syncId, 0, javaContents, ItemStack.EMPTY);
			Client.instance.javaConnection.processServerToClientPacket(inventoryS2CPacket);

			break;
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return InventoryContentPacket.class;
	}

}