package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.protocol.bedrock.data.inventory.InventoryActionData;
import com.nukkitx.protocol.bedrock.data.inventory.InventorySource;
import com.nukkitx.protocol.bedrock.data.inventory.InventorySource.Flag;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.data.inventory.TransactionType;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.utils.ItemDataUtils;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

public class ClickSlotC2SPacketTranslator extends PacketTranslator<ClickSlotC2SPacket> {

	public ClickSlotC2SPacket lastClickedPacket;

	@Override
	public void translate(ClickSlotC2SPacket packet) {
		this.lastClickedPacket = packet;

		/*InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
		
		//0 for main player? container
		inventoryTransactionPacket.getActions().add(new InventoryActionData(InventorySource.fromContainerWindowId(0), slot, fromItem, toItem))
		
		inventoryTransactionPacket.setTransactionType(TransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());
		
		InventorySource inventorySource = InventorySource.fromContainerWindowId(packet.getSyncId());
		int slot = packet.getSlot();*/
		//InventoryActionData inventoryActionData = new InventoryActionData(inventorySource, slot, fromItem, toItem)
		//inventoryTransactionPacket.getActions().add(inventoryActionData);

	}

	@Override
	public Class<?> getPacketClass() {
		return ClickSlotC2SPacket.class;
	}

	//MixinScreenHandler
	public void onCursorStackClickEmptySlot(int clickedSlotId) {

		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();

		inventoryTransactionPacket.setTransactionType(TransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());

		{
			BedrockContainer cursorContainer = Client.instance.containers.getPlayerContainerCursorContainer();

			BedrockContainer containerForClickedSlot = null;
			if (clickedSlotId >= 9 && clickedSlotId <= 44) {//java main inventory slot ids
				containerForClickedSlot = Client.instance.containers.getPlayerInventory();
			} else if (clickedSlotId >= 5 && clickedSlotId <= 8) {//java armor slot ids
				containerForClickedSlot = Client.instance.containers.getPlayerArmorContainer();
			} else if (clickedSlotId == 45) {//java offhand slot id
				containerForClickedSlot = Client.instance.containers.getPlayerOffhandContainer();
			}

			if (containerForClickedSlot == null) {
				System.out.println("FIX THIS, unknown slot clicked " + clickedSlotId);
				return;
			}

			int bedrockSlotId = containerForClickedSlot.convertJavaSlotIdToBedrockSlotId(clickedSlotId);

			ItemData cursorItemData = cursorContainer.getItemFromSlot(0);

			{
				InventoryActionData changeCursorStackToAir = new InventoryActionData(InventorySource.fromContainerWindowId(cursorContainer.getId()), 0, cursorItemData, ItemData.AIR);
				inventoryTransactionPacket.getActions().add(changeCursorStackToAir);
				cursorContainer.setItemBedrock(0, ItemData.AIR);
			}

			{
				//changes it to the cursor slot stack
				InventoryActionData changeClickedSlotItem = new InventoryActionData(InventorySource.fromContainerWindowId(containerForClickedSlot.getId()), bedrockSlotId, ItemData.AIR, cursorItemData);
				inventoryTransactionPacket.getActions().add(changeClickedSlotItem);
				containerForClickedSlot.setItemBedrock(bedrockSlotId, cursorItemData);
			}

		}

		Client.instance.sendPacket(inventoryTransactionPacket);

	}

	public void onEmptyCursorClickStack(int clickedSlotId) {

		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();

		inventoryTransactionPacket.setTransactionType(TransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());

		{
			BedrockContainer cursorContainer = Client.instance.containers.getPlayerContainerCursorContainer();

			BedrockContainer containerForClickedSlot = null;
			if (clickedSlotId >= 9 && clickedSlotId <= 44) {//java main inventory slot ids
				containerForClickedSlot = Client.instance.containers.getPlayerInventory();
			} else if (clickedSlotId >= 5 && clickedSlotId <= 8) {//java armor slot ids
				containerForClickedSlot = Client.instance.containers.getPlayerArmorContainer();
			} else if (clickedSlotId == 45) {//java offhand slot id
				containerForClickedSlot = Client.instance.containers.getPlayerOffhandContainer();
			}

			if (containerForClickedSlot == null) {
				System.out.println("FIX THIS, unknown slot clicked " + clickedSlotId);
				return;
			}

			int bedrockSlotId = containerForClickedSlot.convertJavaSlotIdToBedrockSlotId(clickedSlotId);

			ItemData clickedSlotItemData = containerForClickedSlot.getItemFromSlot(bedrockSlotId);

			{
				InventoryActionData changeClickedStackToAir = new InventoryActionData(InventorySource.fromContainerWindowId(containerForClickedSlot.getId()), bedrockSlotId, clickedSlotItemData, ItemData.AIR);
				inventoryTransactionPacket.getActions().add(changeClickedStackToAir);
				containerForClickedSlot.setItemBedrock(bedrockSlotId, ItemData.AIR);
			}

			{
				InventoryActionData moveClickedStackToCursorContainer = new InventoryActionData(InventorySource.fromContainerWindowId(cursorContainer.getId()), 0, ItemData.AIR, clickedSlotItemData);
				inventoryTransactionPacket.getActions().add(moveClickedStackToCursorContainer);
				cursorContainer.setItemBedrock(0, clickedSlotItemData);
			}

		}

		Client.instance.sendPacket(inventoryTransactionPacket);

	}

	public void onHoverOverStackDropItem(int clickedSlotId, int clickData) {

		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();

		inventoryTransactionPacket.setTransactionType(TransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(TunnelMC.mc.player.getEntityId());

		BedrockContainer containerForClickedSlot = null;
		if (clickedSlotId >= 9 && clickedSlotId <= 44) {//java main inventory slot ids
			containerForClickedSlot = Client.instance.containers.getPlayerInventory();
		} else if (clickedSlotId >= 5 && clickedSlotId <= 8) {//java armor slot ids
			containerForClickedSlot = Client.instance.containers.getPlayerArmorContainer();
		} else if (clickedSlotId == 45) {//java offhand slot id
			containerForClickedSlot = Client.instance.containers.getPlayerOffhandContainer();
		}

		if (containerForClickedSlot == null) {
			System.out.println("FIX THIS, unknown slot clicked " + clickedSlotId);
			return;
		}

		int bedrockSlotId = containerForClickedSlot.convertJavaSlotIdToBedrockSlotId(clickedSlotId);

		ItemData droppedSlotItemData = containerForClickedSlot.getItemFromSlot(bedrockSlotId);
		ItemData afterDropSlotItemData = null;
		if (clickData == 0) {//1 item is dropped
			afterDropSlotItemData = ItemDataUtils.copyWithCount(droppedSlotItemData, droppedSlotItemData.getCount() - 1);
		} else {//all items
			afterDropSlotItemData = ItemData.AIR;
		}

		{
			InventoryActionData decreaseClickedStack = new InventoryActionData(InventorySource.fromContainerWindowId(containerForClickedSlot.getId()), bedrockSlotId, droppedSlotItemData, afterDropSlotItemData);
			inventoryTransactionPacket.getActions().add(decreaseClickedStack);
		}

		{
			int droppedItemCount = clickData == 0 ? 1 : droppedSlotItemData.getCount();
			ItemData itemDroppedInTheWorld = ItemDataUtils.copyWithCount(droppedSlotItemData, droppedItemCount);

			InventoryActionData dropItemInWorld = new InventoryActionData(InventorySource.fromWorldInteraction(Flag.DROP_ITEM), 0, ItemData.AIR, itemDroppedInTheWorld);
			inventoryTransactionPacket.getActions().add(dropItemInWorld);
		}

		containerForClickedSlot.setItemBedrock(bedrockSlotId, afterDropSlotItemData);

		Client.instance.sendPacket(inventoryTransactionPacket);
	}

}