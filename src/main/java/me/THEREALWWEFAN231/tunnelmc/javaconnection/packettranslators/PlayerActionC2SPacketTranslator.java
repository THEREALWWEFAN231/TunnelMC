package me.THEREALWWEFAN231.tunnelmc.javaconnection.packettranslators;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.inventory.TransactionType;
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerActionPacket;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.ServerInventoryCache;
import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.Direction;

public class PlayerActionC2SPacketTranslator extends PacketTranslator<PlayerActionC2SPacket> {

	private Direction lastDirection;
	private Vector3i lastBlockPosition;

	@Override
	public void translate(PlayerActionC2SPacket packet) {

		int runtimeId = TunnelMC.mc.player.getEntityId();

		Vector3i blockPosition = Vector3i.from(packet.getPos().getX(), packet.getPos().getY(), packet.getPos().getZ());
		if (packet.getAction() == Action.START_DESTROY_BLOCK) {
			this.lastDirection = packet.getDirection();
			this.lastBlockPosition = blockPosition;

			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(runtimeId);
			playerActionPacket.setAction(PlayerActionPacket.Action.START_BREAK);
			playerActionPacket.setBlockPosition(blockPosition);
			playerActionPacket.setFace(packet.getDirection().ordinal());

			Client.instance.sendPacket(playerActionPacket);

			EventManager.register(this);
		} else if (packet.getAction() == Action.STOP_DESTROY_BLOCK) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(runtimeId);
			playerActionPacket.setAction(PlayerActionPacket.Action.STOP_BREAK);
			playerActionPacket.setBlockPosition(blockPosition);
			playerActionPacket.setFace(packet.getDirection().ordinal());

			Client.instance.sendPacket(playerActionPacket);

			this.lastDirection = null;
			this.lastBlockPosition = null;
			EventManager.unregister(this);

			InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
			inventoryTransactionPacket.setTransactionType(TransactionType.ITEM_USE);
			inventoryTransactionPacket.setActionType(2);
			inventoryTransactionPacket.setBlockPosition(blockPosition);
			inventoryTransactionPacket.setBlockFace(packet.getDirection().ordinal());
			inventoryTransactionPacket.setHotbarSlot(TunnelMC.mc.player.inventory.selectedSlot);
			inventoryTransactionPacket.setItemInHand(ServerInventoryCache.getItemFromInventory(0, 36 + TunnelMC.mc.player.inventory.selectedSlot));
			inventoryTransactionPacket.setPlayerPosition(Vector3f.from(TunnelMC.mc.player.getPos().x, TunnelMC.mc.player.getPos().y, TunnelMC.mc.player.getPos().z));
			inventoryTransactionPacket.setClickPosition(Vector3f.from(0, 0, 0));

			Client.instance.sendPacket(inventoryTransactionPacket);

		} else if (packet.getAction() == Action.ABORT_DESTROY_BLOCK) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(runtimeId);
			playerActionPacket.setAction(PlayerActionPacket.Action.ABORT_BREAK);
			playerActionPacket.setBlockPosition(blockPosition);
			playerActionPacket.setFace(packet.getDirection().ordinal());

			Client.instance.sendPacket(playerActionPacket);

			this.lastDirection = null;
			this.lastBlockPosition = null;
			EventManager.unregister(this);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return PlayerActionC2SPacket.class;
	}

	@EventTarget
	public void event(EventPlayerTick event) {
		int runtimeId = TunnelMC.mc.player.getEntityId();
		PlayerActionPacket.Action action = PlayerActionPacket.Action.CONTINUE_BREAK;

		PlayerActionPacket playerActionPacket = new PlayerActionPacket();
		playerActionPacket.setRuntimeEntityId(runtimeId);
		playerActionPacket.setAction(action);
		playerActionPacket.setBlockPosition(this.lastBlockPosition);
		playerActionPacket.setFace(this.lastDirection.ordinal());

		Client.instance.sendPacket(playerActionPacket);
	}

}
