package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.packet.MobEquipmentPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
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
		
		if(hotbarSlot < 0 || hotbarSlot > 8) {
			System.out.println("Can not send an invalid hotbar slot");
			return;
		}
		
		long runtimeEntityId = TunnelMC.mc.player.getId();
		BedrockContainer container = Client.instance.containers.getPlayerInventory();
		
		ItemData item = container.getItemFromSlot(hotbarSlot);
		
		MobEquipmentPacket mobEquipmentPacket = new MobEquipmentPacket();
		mobEquipmentPacket.setRuntimeEntityId(runtimeEntityId);
		mobEquipmentPacket.setItem(item);
		mobEquipmentPacket.setInventorySlot(hotbarSlot);
		mobEquipmentPacket.setHotbarSlot(hotbarSlot);
		mobEquipmentPacket.setContainerId(container.getId());

		Client.instance.sendPacket(mobEquipmentPacket);
	}

}
