package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.packet.MobEquipmentPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.ServerInventoryCache;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class UpdateSelectedSlotC2SPacketTranslator extends PacketTranslator<UpdateSelectedSlotC2SPacket> {

	@Override
	public void translate(UpdateSelectedSlotC2SPacket packet) {
		UpdateSelectedSlotC2SPacketTranslator.updateHotbarItem(packet.getSelectedSlot());
	}

	@Override
	public Class<?> getPacketClass() {
		return UpdateSelectedSlotC2SPacket.class;
	}
	
	public static void updateHotbarItem(int hotbarSlot) {
		
		int javaInventorySlot = hotbarSlot;
		if (javaInventorySlot < 9) {
			javaInventorySlot += 36;
		}
		
		long runtimeEntityId = TunnelMC.mc.player.getEntityId();
		ItemData item = ServerInventoryCache.getItemFromInventory(ServerInventoryCache.JAVA_MAIN_INVENTORY_ID, javaInventorySlot);
		
		MobEquipmentPacket mobEquipmentPacket = new MobEquipmentPacket();
		mobEquipmentPacket.setRuntimeEntityId(runtimeEntityId);
		mobEquipmentPacket.setItem(item);
		mobEquipmentPacket.setInventorySlot(hotbarSlot);
		mobEquipmentPacket.setHotbarSlot(hotbarSlot);
		mobEquipmentPacket.setContainerId(ServerInventoryCache.BEDROCK_MAIN_INVENTORY_ID);

		Client.instance.sendPacket(mobEquipmentPacket);
	}

}
