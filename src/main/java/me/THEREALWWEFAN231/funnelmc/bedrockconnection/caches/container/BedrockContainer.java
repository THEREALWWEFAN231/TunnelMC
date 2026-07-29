package me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container;

import java.util.ArrayList;

import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

public class BedrockContainer {

	protected int size;
	protected ArrayList<ItemData> items;
	protected int id;

	public BedrockContainer(int size, int id) {
		this.size = size;
		this.id = id;

		this.items = new ArrayList<ItemData>();
		for (int i = 0; i < size; i++) {
			this.items.add(ItemData.AIR);
		}
	}

	public void setItemBedrock(int slot, ItemData itemData) {
		this.ensureCapacity(slot);
		this.items.set(slot, itemData);
	}

	public void setItemFromJavaSlot(int javaSlot, ItemData itemData) {
		this.ensureCapacity(javaSlot);
		this.items.set(javaSlot, itemData);
	}

	// Some container ids (e.g. ContainerId.OFFHAND, ContainerId.UI) aren't a fixed single-purpose
	// slot count the way this class assumes at construction - UI in particular is Bedrock's general-
	// purpose window used for whatever "special" slots the currently open UI needs (crafting grid,
	// cursor, ...), so its real slot range varies by context and isn't knowable up front. Grow instead
	// of crashing when a slot beyond the size we guessed at construction shows up.
	private void ensureCapacity(int slot) {
		while (this.items.size() <= slot) {
			this.items.add(ItemData.AIR);
		}
		this.size = Math.max(this.size, this.items.size());
	}
	
	public int convertJavaSlotIdToBedrockSlotId(int javaSlotId) {
		return 0;
	}

	public ItemData getItemFromSlot(int slot) {
		if (slot >= this.items.size()) {
			return ItemData.AIR;
		}
		return this.items.get(slot);
	}

	public int getSize() {
		return this.size;
	}

	public ArrayList<ItemData> getItems() {
		return this.items;
	}

	public int getId() {
		return this.id;
	}

}
