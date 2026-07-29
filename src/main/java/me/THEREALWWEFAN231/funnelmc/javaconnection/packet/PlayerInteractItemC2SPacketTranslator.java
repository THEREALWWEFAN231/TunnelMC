package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.world.entity.Pose;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class PlayerInteractItemC2SPacketTranslator extends PacketTranslator<ServerboundUseItemPacket> {

	//TODO: im not even fully sure about this, i dont really have any means to test it currently
	//actually i think i could test it on a chest, i should do that sometime

	@Override
	public void translate(ServerboundUseItemPacket packet) {

		ItemData usingItem = Client.instance.containers.getPlayerInventory().getItemFromSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());

		if (FunnelMC.mc.hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult) FunnelMC.mc.hitResult).getBlockPos();
			Vector3i blockPosition = Vector3i.from(blockPos.getX(), blockPos.getY(), blockPos.getZ());

			Vec3 sideHitOffset = ((BlockHitResult) FunnelMC.mc.hitResult).getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());

			InventoryTransactionPacket useInventoryTransactionPacket = new InventoryTransactionPacket();
			useInventoryTransactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
			useInventoryTransactionPacket.setActionType(0);
			useInventoryTransactionPacket.setBlockPosition(blockPosition);
			useInventoryTransactionPacket.setBlockFace(((BlockHitResult) FunnelMC.mc.hitResult).getDirection().ordinal());
			useInventoryTransactionPacket.setHotbarSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());
			useInventoryTransactionPacket.setItemInHand(usingItem);
			useInventoryTransactionPacket.setPlayerPosition(Vector3f.from(FunnelMC.mc.player.getX(), FunnelMC.mc.player.getY() + FunnelMC.mc.player.getEyeHeight(Pose.STANDING), FunnelMC.mc.player.getZ()));
			useInventoryTransactionPacket.setClickPosition(Vector3f.from(sideHitOffset.x, sideHitOffset.y, sideHitOffset.z));
			Client.instance.sendPacket(useInventoryTransactionPacket);

		} else {
			//they used the item in air

			InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
			inventoryTransactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
			inventoryTransactionPacket.setActionType(1);
			inventoryTransactionPacket.setBlockPosition(Vector3i.ZERO);
			inventoryTransactionPacket.setBlockFace(255);
			inventoryTransactionPacket.setHotbarSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());
			inventoryTransactionPacket.setItemInHand(usingItem);
			inventoryTransactionPacket.setPlayerPosition(Vector3f.from(FunnelMC.mc.player.getX(), FunnelMC.mc.player.getY() + FunnelMC.mc.player.getEyeHeight(Pose.STANDING), FunnelMC.mc.player.getZ()));
			inventoryTransactionPacket.setClickPosition(Vector3f.ZERO);

			Client.instance.sendPacket(inventoryTransactionPacket);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundUseItemPacket.class;
	}

}
