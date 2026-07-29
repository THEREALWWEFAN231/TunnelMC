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
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class PlayerInteractBlockC2SPacketTranslator extends PacketTranslator<ServerboundUseItemOnPacket> {

	//TODO: so when ever we jump then place a block under us, our head freaks out(to other players), on nukkit servers(probably all), not fully sure why, maybe because we aren't sending PlayerActionPacket.JUMP

	@Override
	public void translate(ServerboundUseItemOnPacket packet) {

		BlockPos blockPos = packet.getHitResult().getBlockPos();
		Vector3i blockPosition = Vector3i.from(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		Vec3 sideHitOffset = packet.getHitResult().getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());

		ItemData placingItem = Client.instance.containers.getPlayerInventory().getItemFromSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());

		InventoryTransactionPacket placeInventoryTransactionPacket = new InventoryTransactionPacket();
		placeInventoryTransactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
		placeInventoryTransactionPacket.setActionType(0);
		placeInventoryTransactionPacket.setBlockPosition(blockPosition);
		placeInventoryTransactionPacket.setBlockFace(packet.getHitResult().getDirection().ordinal());
		placeInventoryTransactionPacket.setHotbarSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());
		placeInventoryTransactionPacket.setItemInHand(placingItem);
		placeInventoryTransactionPacket.setPlayerPosition(Vector3f.from(FunnelMC.mc.player.getX(), FunnelMC.mc.player.getY() + FunnelMC.mc.player.getEyeHeight(Pose.STANDING), FunnelMC.mc.player.getZ()));
		placeInventoryTransactionPacket.setClickPosition(Vector3f.from(sideHitOffset.x, sideHitOffset.y, sideHitOffset.z));
		Client.instance.sendPacket(placeInventoryTransactionPacket);

		//when using proxy pass and spying on the client it sends 2 InventoryTransactionPackets
		InventoryTransactionPacket idkInventoryTransactionPacket = new InventoryTransactionPacket();
		idkInventoryTransactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
		idkInventoryTransactionPacket.setActionType(1);
		idkInventoryTransactionPacket.setBlockPosition(Vector3i.ZERO);
		idkInventoryTransactionPacket.setBlockFace(255);
		idkInventoryTransactionPacket.setHotbarSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());
		idkInventoryTransactionPacket.setItemInHand(placingItem);
		idkInventoryTransactionPacket.setPlayerPosition(Vector3f.from(FunnelMC.mc.player.getX(), FunnelMC.mc.player.getY() + FunnelMC.mc.player.getEyeHeight(Pose.STANDING), FunnelMC.mc.player.getZ()));
		idkInventoryTransactionPacket.setClickPosition(Vector3f.ZERO);

		Client.instance.sendPacket(idkInventoryTransactionPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundUseItemOnPacket.class;
	}

}
