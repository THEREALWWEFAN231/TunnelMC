package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public class PlayerInteractEntityC2SPacketTranslator extends PacketTranslator<ServerboundInteractPacket>{

	@Override
	public void translate(ServerboundInteractPacket packet) {

		ItemData holdingItem = Client.instance.containers.getPlayerInventory().getItemFromSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());

		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
		inventoryTransactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE_ON_ENTITY);
		inventoryTransactionPacket.setActionType(1);
		inventoryTransactionPacket.setRuntimeEntityId(packet.entityId());
		inventoryTransactionPacket.setHotbarSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());
		inventoryTransactionPacket.setItemInHand(holdingItem);
		inventoryTransactionPacket.setPlayerPosition(Vector3f.from(FunnelMC.mc.player.getX(), FunnelMC.mc.player.getY() + FunnelMC.mc.player.getEyeHeight(Pose.STANDING), FunnelMC.mc.player.getZ()));
		inventoryTransactionPacket.setClickPosition(Vector3f.ZERO);

		Client.instance.sendPacket(inventoryTransactionPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundInteractPacket.class;
	}

}
