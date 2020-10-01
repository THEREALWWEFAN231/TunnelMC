package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.data.inventory.TransactionType;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.ServerInventoryCache;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class PlayerInteractEntityC2SPacketTranslator extends PacketTranslator<PlayerInteractEntityC2SPacket>{

	@Override
	public void translate(PlayerInteractEntityC2SPacket packet) {
		
		ItemData holdingItem = ServerInventoryCache.getItemFromInventory(0, 36 + TunnelMC.mc.player.inventory.selectedSlot);
		
		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
		inventoryTransactionPacket.setTransactionType(TransactionType.ITEM_USE_ON_ENTITY);
		inventoryTransactionPacket.setActionType(1);
		inventoryTransactionPacket.setRuntimeEntityId(packet.getEntity(TunnelMC.mc.world).getEntityId());
		inventoryTransactionPacket.setHotbarSlot(TunnelMC.mc.player.inventory.selectedSlot);
		inventoryTransactionPacket.setItemInHand(holdingItem);
		inventoryTransactionPacket.setPlayerPosition(Vector3f.from(TunnelMC.mc.player.getPos().x, TunnelMC.mc.player.getPos().y + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING), TunnelMC.mc.player.getPos().z));
		inventoryTransactionPacket.setClickPosition(Vector3f.ZERO);
		
		Client.instance.sendPacket(inventoryTransactionPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerInteractEntityC2SPacket.class;
	}

}
