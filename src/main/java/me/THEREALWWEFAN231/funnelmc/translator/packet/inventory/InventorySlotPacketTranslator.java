package me.THEREALWWEFAN231.funnelmc.translator.packet.inventory;

import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.funnelmc.javaconnection.packet.UpdateSelectedSlotC2SPacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.item.ItemTranslator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

public class InventorySlotPacketTranslator extends PacketTranslator<InventorySlotPacket> {

	@Override
	public void translate(InventorySlotPacket packet) {

		int syncId = packet.getContainerId();
		BedrockContainer containerToChange = Client.instance.containers.getContainers().get(syncId);
		if(containerToChange == null) {//TODO: create some sort of "temp" container, we use to do this, but for testing purposes this does for now
			System.out.println("Couldn't find container with id " + syncId);
			return;
		}

		int javaInventorySlot = packet.getSlot();
		int packetSlot = packet.getSlot();
		ItemStack stack = ItemTranslator.itemDataToItemStack(packet.getItem());

		if (syncId == 0) {//TODO: change this/find a better way
			if (javaInventorySlot < 9) {
				javaInventorySlot += 36;
			}
		}

		ClientboundContainerSetSlotPacket handlerSlotUpdateS2CPacket = new ClientboundContainerSetSlotPacket(syncId, 0, javaInventorySlot, stack);
		Client.instance.javaConnection.processServerToClientPacket(handlerSlotUpdateS2CPacket);

		containerToChange.setItemBedrock(packet.getSlot(), packet.getItem());

		//not fully sure if "vanilla" bedrock does it like this, but for example, we could be at slot 0, and get a new item in that slot, and we are still holding nothing, so we have to update our held item, this is stupid though, it should be server side
		if (packetSlot == FunnelMC.mc.player.getInventory().getSelectedSlot()) {
			UpdateSelectedSlotC2SPacketTranslator.updateHotbarItem(packetSlot);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return InventorySlotPacket.class;
	}

	@Override
	public boolean idleUntil() {
		return FunnelMC.mc.player != null;
	}

}
