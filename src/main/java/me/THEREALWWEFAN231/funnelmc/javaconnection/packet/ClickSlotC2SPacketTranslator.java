package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource.Flag;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.container.screenhandler.ScreenHandlerTranslatorManager;
import me.THEREALWWEFAN231.funnelmc.utils.ItemDataUtils;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ClickSlotC2SPacketTranslator extends PacketTranslator<ServerboundContainerClickPacket> {

	@Override
	public void translate(ServerboundContainerClickPacket packet) {

	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundContainerClickPacket.class;
	}

	//MixinScreenHandler
	public void onCursorStackClickEmptySlot(AbstractContainerMenu screenHandler, int clickedSlotId, int itemCountToMoveFromCursorToClickedSlot) {

		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();

		inventoryTransactionPacket.setTransactionType(InventoryTransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());

		{
			BedrockContainer cursorContainer = Client.instance.containers.getPlayerContainerCursorContainer();
			BedrockContainer containerForClickedSlot = ScreenHandlerTranslatorManager.getBedrockContainerFromJava(screenHandler, clickedSlotId);

			if (containerForClickedSlot == null) {
				System.out.println("FIX THIS, unknown slot clicked " + clickedSlotId);
				return;
			}

			int bedrockSlotId = ScreenHandlerTranslatorManager.getBedrockSlotFromJavaContainer(screenHandler, clickedSlotId, containerForClickedSlot);

			ItemData cursorItemData = cursorContainer.getItemFromSlot(0);

			{
				//decrease if the user right clicked a slot, change to air if they left clicked a slot
				ItemData decreasedCursorStack = ItemDataUtils.copyWithCount(cursorItemData, cursorItemData.getCount() - itemCountToMoveFromCursorToClickedSlot);
				if (decreasedCursorStack.getCount() == 0) {
					decreasedCursorStack = ItemData.AIR;
				}

				InventoryActionData decreaseCursorStack = new InventoryActionData(InventorySource.fromContainerWindowId(cursorContainer.getId()), 0, cursorItemData, decreasedCursorStack);
				inventoryTransactionPacket.getActions().add(decreaseCursorStack);
				cursorContainer.setItemBedrock(0, decreasedCursorStack);
			}

			{
				ItemData clickedSlotNewItemData = ItemDataUtils.copyWithCount(cursorItemData, itemCountToMoveFromCursorToClickedSlot);

				//changes it to the cursor slot stack
				InventoryActionData incrementClickedSlotWithCursorStack = new InventoryActionData(InventorySource.fromContainerWindowId(containerForClickedSlot.getId()), bedrockSlotId, ItemData.AIR, clickedSlotNewItemData);
				inventoryTransactionPacket.getActions().add(incrementClickedSlotWithCursorStack);
				containerForClickedSlot.setItemBedrock(bedrockSlotId, clickedSlotNewItemData);
			}

		}

		Client.instance.sendPacket(inventoryTransactionPacket);

	}

	public void onEmptyCursorClickStack(AbstractContainerMenu screenHandler, int clickedSlotId) {
		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();

		inventoryTransactionPacket.setTransactionType(InventoryTransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());

		{
			BedrockContainer cursorContainer = Client.instance.containers.getPlayerContainerCursorContainer();
			BedrockContainer containerForClickedSlot = ScreenHandlerTranslatorManager.getBedrockContainerFromJava(screenHandler, clickedSlotId);

			if (containerForClickedSlot == null) {
				System.out.println("FIX THIS, unknown slot clicked " + clickedSlotId);
				return;
			}

			int bedrockSlotId = ScreenHandlerTranslatorManager.getBedrockSlotFromJavaContainer(screenHandler, clickedSlotId, containerForClickedSlot);

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

	public void onHoverOverStackDropItem(AbstractContainerMenu screenHandler, int clickedSlotId, int clickData) {

		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();

		inventoryTransactionPacket.setTransactionType(InventoryTransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());

		BedrockContainer containerForClickedSlot = ScreenHandlerTranslatorManager.getBedrockContainerFromJava(screenHandler, clickedSlotId);

		if (containerForClickedSlot == null) {
			System.out.println("FIX THIS, unknown slot clicked " + clickedSlotId);
			return;
		}

		int bedrockSlotId = ScreenHandlerTranslatorManager.getBedrockSlotFromJavaContainer(screenHandler, clickedSlotId, containerForClickedSlot);

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

	public void onStackShiftClicked(AbstractContainerMenu screenHandler, int clickedSlotId) {

	}

	public void onCursorStackAddToStack(AbstractContainerMenu screenHandler, int clickedSlotId) {//for example the user has 64 oak planks in the cursor, and they right click a slot with oak planks(not an empty slot)
		/*InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
		
		inventoryTransactionPacket.setTransactionType(InventoryTransactionType.NORMAL);
		inventoryTransactionPacket.setActionType(0);//I have no idea
		inventoryTransactionPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());
		
		BedrockContainer containerForClickedSlot = JavaContainerFinder.getContainerFromJava(screenHandler, clickedSlotId);
		
		if (containerForClickedSlot == null) {
			System.out.println("FIX THIS, unknown slot clicked " + clickedSlotId);
			return;
		}
		
		int bedrockSlotId = containerForClickedSlot.convertJavaSlotIdToBedrockSlotId(clickedSlotId);
		bedrockSlotId = JavaContainerFinder.getBedrockSlotFromJavaContainer(screenHandler, clickedSlotId, containerForClickedSlot);
		
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
		
		Client.instance.sendPacket(inventoryTransactionPacket);*/
	}

}