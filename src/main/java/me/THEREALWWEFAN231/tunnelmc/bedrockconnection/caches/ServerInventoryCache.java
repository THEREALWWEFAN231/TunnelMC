package me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches;

import java.util.HashMap;

import com.nukkitx.protocol.bedrock.data.inventory.ItemData;

public class ServerInventoryCache {

	public static final int JAVA_MAIN_INVENTORY_ID = 0;
	public static final int BEDROCK_MAIN_INVENTORY_ID = 0;

	//inventory id and the inventory
	private static final HashMap<Integer, BedrockInventory> INVENTORIES = new HashMap<Integer, BedrockInventory>();

	public static void putItemInInventory(int inventoryId, int slotId, ItemData itemData) {
		BedrockInventory inventory = INVENTORIES.get(inventoryId);
		if (inventory == null) {
			inventory = new BedrockInventory();
			INVENTORIES.put(inventoryId, inventory);
		}

		inventory.contents.put(slotId, itemData);

	}

	public static ItemData getItemFromInventory(int inventoryId, int slotId) {
		BedrockInventory inventory = INVENTORIES.get(inventoryId);
		if (inventory == null) {
			return ItemData.AIR;
		}

		ItemData theItem = inventory.contents.get(slotId);
		if (theItem == null) {
			return ItemData.AIR;
		}

		return theItem;
	}

	public static class BedrockInventory {

		public final HashMap<Integer, ItemData> contents = new HashMap<Integer, ItemData>();

	}
}
