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
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlayerInteractBlockC2SPacketTranslator extends PacketTranslator<PlayerInteractBlockC2SPacket> {

	//TODO: so when ever we jump then place a block under us, our head freaks out(to other players), on nukkit servers(probably all), not fully sure why, maybe because we aren't sending PlayerActionPacket.JUMP

	@Override
	public void translate(PlayerInteractBlockC2SPacket packet) {

		BlockPos blockPos = packet.getBlockHitResult().getBlockPos();
		Vector3i blockPosition = Vector3i.from(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		Vec3d sideHitOffset = packet.getBlockHitResult().getPos().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());

		ItemData placingItem = Client.instance.containers.getPlayerInventory().getItemFromSlot(TunnelMC.mc.player.getInventory().selectedSlot);

		InventoryTransactionPacket placeInventoryTransactionPacket = new InventoryTransactionPacket();
		placeInventoryTransactionPacket.setTransactionType(TransactionType.ITEM_USE);
		placeInventoryTransactionPacket.setActionType(0);
		placeInventoryTransactionPacket.setBlockPosition(blockPosition);
		placeInventoryTransactionPacket.setBlockFace(packet.getBlockHitResult().getSide().ordinal());
		placeInventoryTransactionPacket.setHotbarSlot(TunnelMC.mc.player.getInventory().selectedSlot);
		placeInventoryTransactionPacket.setItemInHand(placingItem);
		placeInventoryTransactionPacket.setPlayerPosition(Vector3f.from(TunnelMC.mc.player.getPos().x, TunnelMC.mc.player.getPos().y + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING), TunnelMC.mc.player.getPos().z));
		placeInventoryTransactionPacket.setClickPosition(Vector3f.from(sideHitOffset.x, sideHitOffset.y, sideHitOffset.z));
		placeInventoryTransactionPacket.setBlockRuntimeId(0);//TODO: get the runtime id of the block we are holding(i actually think its the block we are right clicking not holding, in that case its easier), currently works(on nukkit) with it being zero, but we *should* do it correctly
		Client.instance.sendPacket(placeInventoryTransactionPacket);

		//when using proxy pass and spying on the client it sends 2 InventoryTransactionPackets
		InventoryTransactionPacket idkInventoryTransactionPacket = new InventoryTransactionPacket();
		idkInventoryTransactionPacket.setTransactionType(TransactionType.ITEM_USE);
		idkInventoryTransactionPacket.setActionType(1);
		idkInventoryTransactionPacket.setBlockPosition(Vector3i.ZERO);
		idkInventoryTransactionPacket.setBlockFace(255);
		idkInventoryTransactionPacket.setHotbarSlot(TunnelMC.mc.player.getInventory().selectedSlot);
		idkInventoryTransactionPacket.setItemInHand(placingItem);
		idkInventoryTransactionPacket.setPlayerPosition(Vector3f.from(TunnelMC.mc.player.getPos().x, TunnelMC.mc.player.getPos().y + TunnelMC.mc.player.getEyeHeight(EntityPose.STANDING), TunnelMC.mc.player.getPos().z));
		idkInventoryTransactionPacket.setClickPosition(Vector3f.ZERO);
		idkInventoryTransactionPacket.setBlockRuntimeId(0);

		Client.instance.sendPacket(idkInventoryTransactionPacket);

	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerInteractBlockC2SPacket.class;
	}

}
