package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.nukkitx.protocol.bedrock.data.inventory.InventorySource;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

public class ClickSlotC2SPacketTranslator extends PacketTranslator<ClickSlotC2SPacket> {

	@Override
	@SuppressWarnings("unused")
	public void translate(ClickSlotC2SPacket packet) {
		
		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
		
		InventorySource inventorySource = InventorySource.fromContainerWindowId(packet.getSyncId());
		int slot = packet.getSlot();
		//InventoryActionData inventoryActionData = new InventoryActionData(inventorySource, slot, fromItem, toItem)
		//inventoryTransactionPacket.getActions().add(inventoryActionData);
		
	}

	@Override
	public Class<?> getPacketClass() {
		return ClickSlotC2SPacket.class;
	}

}