package me.THEREALWWEFAN231.funnelmc.javaconnection.packet;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;

public class PlayerActionTranslator extends PacketTranslator<ServerboundPlayerActionPacket> {

	private Direction lastDirection;
	private Vector3i lastBlockPosition;

	@Override
	public void translate(ServerboundPlayerActionPacket packet) {

		int runtimeId = FunnelMC.mc.player.getId();

		Vector3i blockPosition = Vector3i.from(packet.getPos().getX(), packet.getPos().getY(), packet.getPos().getZ());
		if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
			this.lastDirection = packet.getDirection();
			this.lastBlockPosition = blockPosition;

			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(runtimeId);
			playerActionPacket.setAction(PlayerActionType.START_BREAK);
			playerActionPacket.setBlockPosition(blockPosition);
			playerActionPacket.setResultPosition(Vector3i.ZERO);
			playerActionPacket.setFace(packet.getDirection().ordinal());

			Client.instance.sendPacket(playerActionPacket);

			EventManager.register(this);
		} else if (packet.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(runtimeId);
			playerActionPacket.setAction(PlayerActionType.STOP_BREAK);
			playerActionPacket.setBlockPosition(blockPosition);
			playerActionPacket.setResultPosition(Vector3i.ZERO);
			playerActionPacket.setFace(packet.getDirection().ordinal());

			Client.instance.sendPacket(playerActionPacket);

			if (Minecraft.getInstance().gameMode.getPlayerMode() == GameType.CREATIVE) {
				//TODO
				PlayerActionPacket creativePacket = new PlayerActionPacket();
				creativePacket.setRuntimeEntityId(runtimeId);
				creativePacket.setAction(PlayerActionType.DIMENSION_CHANGE_REQUEST_OR_CREATIVE_DESTROY_BLOCK);
				creativePacket.setBlockPosition(blockPosition);
				creativePacket.setResultPosition(Vector3i.ZERO);
				playerActionPacket.setFace(packet.getDirection().ordinal());

				Client.instance.sendPacket(playerActionPacket);
			}

			this.lastDirection = null;
			this.lastBlockPosition = null;
			EventManager.unregister(this);

			InventoryTransactionPacket inventoryTransactionPacket = new InventoryTransactionPacket();
			inventoryTransactionPacket.setTransactionType(InventoryTransactionType.ITEM_USE);
			inventoryTransactionPacket.setActionType(2);
			inventoryTransactionPacket.setBlockPosition(blockPosition);
			inventoryTransactionPacket.setBlockFace(packet.getDirection().ordinal());
			inventoryTransactionPacket.setHotbarSlot(FunnelMC.mc.player.getInventory().getSelectedSlot());
			inventoryTransactionPacket.setItemInHand(Client.instance.containers.getPlayerInventory().getItemFromSlot(FunnelMC.mc.player.getInventory().getSelectedSlot()));
			inventoryTransactionPacket.setPlayerPosition(Vector3f.from(FunnelMC.mc.player.getX(), FunnelMC.mc.player.getY(), FunnelMC.mc.player.getZ()));
			inventoryTransactionPacket.setClickPosition(Vector3f.ZERO);

			Client.instance.sendPacket(inventoryTransactionPacket);

		} else if (packet.getAction() == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(runtimeId);
			playerActionPacket.setAction(PlayerActionType.ABORT_BREAK);
			playerActionPacket.setBlockPosition(blockPosition);
			playerActionPacket.setResultPosition(Vector3i.ZERO);
			playerActionPacket.setFace(packet.getDirection().ordinal());

			Client.instance.sendPacket(playerActionPacket);

			this.lastDirection = null;
			this.lastBlockPosition = null;
			EventManager.unregister(this);
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return ServerboundPlayerActionPacket.class;
	}

	@EventTarget
	public void event(EventPlayerTick event) {
		int runtimeId = FunnelMC.mc.player.getId();
		PlayerActionType action = PlayerActionType.CONTINUE_BREAK;

		PlayerActionPacket playerActionPacket = new PlayerActionPacket();
		playerActionPacket.setRuntimeEntityId(runtimeId);
		playerActionPacket.setAction(action);
		playerActionPacket.setBlockPosition(this.lastBlockPosition);
		playerActionPacket.setResultPosition(Vector3i.ZERO);
		playerActionPacket.setFace(this.lastDirection.ordinal());

		Client.instance.sendPacket(playerActionPacket);
	}

}
