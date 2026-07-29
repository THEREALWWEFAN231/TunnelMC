package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public class UpdateSelectedSlotC2SPacketTranslator extends PacketTranslator<ServerboundSetCarriedItemPacket> {

	@Override
	public void translate(ServerboundSetCarriedItemPacket packet) {
		UpdateSelectedSlotC2SPacketTranslator.updateHotbarItem(packet.getSlot());
	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundSetCarriedItemPacket.class;
	}
	
	public static void updateHotbarItem(int hotbarSlot) {
		
		if(hotbarSlot < 0 || hotbarSlot > 8) {
			System.out.println("Can not send an invalid hotbar slot");
			return;
		}
		
		long runtimeEntityId = FunnelMC.mc.player.getId();
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
