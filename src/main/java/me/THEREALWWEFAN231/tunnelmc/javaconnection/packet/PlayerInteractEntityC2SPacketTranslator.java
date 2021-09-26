package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.data.inventory.TransactionType;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.world.World;

public class PlayerInteractEntityC2SPacketTranslator extends PacketTranslator<PlayerInteractEntityC2SPacket>{

	@Override
	public void translate(PlayerInteractEntityC2SPacket packet) {
		
		ItemData holdingItem = Client.instance.containers.getPlayerInventory().getItemFromSlot(TunnelMC.mc.player.getInventory().selectedSlot);
		
		InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
		inventoryTransactionPacket.setTransactionType(TransactionType.ITEM_USE_ON_ENTITY);
		inventoryTransactionPacket.setActionType(1);
		inventoryTransactionPacket.setRuntimeEntityId(packet.getEntity(TunnelMC.mc.world.getServer().getWorld(World.OVERWORLD)).getId());
		inventoryTransactionPacket.setHotbarSlot(TunnelMC.mc.player.getInventory().selectedSlot);
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
