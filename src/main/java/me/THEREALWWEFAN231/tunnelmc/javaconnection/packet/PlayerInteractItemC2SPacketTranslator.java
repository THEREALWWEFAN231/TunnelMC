package me.THEREALWWEFAN231.tunnelmc.javaconnection.packet;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.data.inventory.TransactionType;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlayerInteractItemC2SPacketTranslator extends PacketTranslator<PlayerInteractItemC2SPacket> {

	//TODO: im not even fully sure about this, i dont really have any means to test it currently
	//actually i think i could test it on a chest, i should do that sometime

	@Override
	public void translate(PlayerInteractItemC2SPacket packet) {
		
		ItemData usingItem = Client.instance.containers.getPlayerInventory().getItemFromSlot(TunnelMC.mc.player.getInventory().selectedSlot);

		if (TunnelMC.mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult) TunnelMC.mc.crosshairTarget).getBlockPos();
			Vector3i blockPosition = Vector3i.from(blockPos.getX(), blockPos.getY(), blockPos.getZ());

			Vec3d sideHitOffset = ((BlockHitResult) TunnelMC.mc.crosshairTarget).getPos().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());

			InventoryTransactionPacket useInventoryTransactionPacket = new InventoryTransactionPacket();
			useInventoryTransactionPacket.setTransactionType(TransactionType.ITEM_USE);
			useInventoryTransactionPacket.setActionType(0);
			useInventoryTransactionPacket.setBlockPosition(blockPosition);
			useInventoryTransactionPacket.setBlockFace(((BlockHitResult) TunnelMC.mc.crosshairTarget).getSide().ordinal());
			useInventoryTransactionPacket.setHotbarSlot(TunnelMC.mc.player.getInventory().selectedSlot);
			useInventoryTransactionPacket.setItemInHand(usingItem);
			useInventoryTransactionPacket.setPlayerPosition(Vector3f.from(TunnelMC.mc.player.getPos().x, TunnelMC.mc.player.getPos().y + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING), TunnelMC.mc.player.getPos().z));
			useInventoryTransactionPacket.setClickPosition(Vector3f.from(sideHitOffset.x, sideHitOffset.y, sideHitOffset.z));
			useInventoryTransactionPacket.setBlockRuntimeId(0);//TODO: get the runtime id of the block we are holding(i actually think its the block we are right clicking not holding, in that case its easier), currently works(on nukkit) with it being zero, but we *should* do it correctly
			Client.instance.sendPacket(useInventoryTransactionPacket);

		} else {
			//they used the item in air

			InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
			inventoryTransactionPacket.setTransactionType(TransactionType.ITEM_USE);
			inventoryTransactionPacket.setActionType(1);
			inventoryTransactionPacket.setBlockPosition(Vector3i.ZERO);
			inventoryTransactionPacket.setBlockFace(255);
			inventoryTransactionPacket.setHotbarSlot(TunnelMC.mc.player.getInventory().selectedSlot);
			inventoryTransactionPacket.setItemInHand(usingItem);
			inventoryTransactionPacket.setPlayerPosition(Vector3f.from(TunnelMC.mc.player.getPos().x, TunnelMC.mc.player.getPos().y + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING), TunnelMC.mc.player.getPos().z));
			inventoryTransactionPacket.setClickPosition(Vector3f.ZERO);

			Client.instance.sendPacket(inventoryTransactionPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerInteractItemC2SPacket.class;
	}

}
