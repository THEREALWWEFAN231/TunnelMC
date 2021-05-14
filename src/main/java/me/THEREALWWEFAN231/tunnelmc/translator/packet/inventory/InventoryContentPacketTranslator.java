package me.THEREALWWEFAN231.tunnelmc.translator.packet.inventory;

import com.nukkitx.protocol.bedrock.packet.InventoryContentPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.item.ItemTranslator;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.collection.DefaultedList;

public class InventoryContentPacketTranslator extends PacketTranslator<InventoryContentPacket> {

	@Override
	public void translate(InventoryContentPacket packet) {

		int syncId = packet.getContainerId();
		int playerInventorySize = packet.getContents().size();
		if(syncId == 0) {
			playerInventorySize = TunnelMC.mc.player.playerScreenHandler.slots.size();
		}
		BedrockContainer containerToChange = Client.instance.containers.getContainers().get(syncId);
		if(containerToChange == null) {//TODO: create some sort of "temp" container, we use to do this, but for testing purposes this does for now
			System.out.println("Couldn't find container with id " + syncId);
			return;
		}
		
		
		DefaultedList<ItemStack> contents = DefaultedList.ofSize(playerInventorySize, ItemStack.EMPTY);
		for (int i = 0; i < packet.getContents().size(); i++) {
			ItemStack itemStack = ItemTranslator.itemDataToItemStack(packet.getContents().get(i));

			
			int javaInventorySlot = i;
			if (syncId == 0) {
				if (javaInventorySlot < 9) {
					javaInventorySlot += 36;
				}
			}
			
			contents.set(javaInventorySlot, itemStack);
			containerToChange.setItemBedrock(i, packet.getContents().get(i));
		}

		InventoryS2CPacket inventoryS2CPacket = new InventoryS2CPacket(syncId, contents);
		Client.instance.javaConnection.processServerToClientPacket(inventoryS2CPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return InventoryContentPacket.class;
	}

}