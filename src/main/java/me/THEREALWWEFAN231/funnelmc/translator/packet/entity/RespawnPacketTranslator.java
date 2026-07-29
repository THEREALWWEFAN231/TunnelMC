package me.THEREALWWEFAN231.funnelmc.translator.packet.entity;

import java.util.Optional;

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket.State;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.dimension.DimensionTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class RespawnPacketTranslator extends PacketTranslator<RespawnPacket> {

	@Override
	public void translate(RespawnPacket packet) {

		if (packet.getState() == State.SERVER_SEARCHING) {//display death screen?

		}

		if (packet.getState() == State.SERVER_READY) {

			PlayerActionPacket playerActionPacket = new PlayerActionPacket();
			playerActionPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());
			playerActionPacket.setAction(PlayerActionType.RESPAWN);
			playerActionPacket.setBlockPosition(Vector3i.ZERO);
			// The serializer writes this unconditionally regardless of action type, even though it's
			// only meaningful for block-interaction actions - never set here, so RESPAWN NPE'd trying
			// to encode a null resultPosition.
			playerActionPacket.setResultPosition(Vector3i.ZERO);
			playerActionPacket.setFace(-1);

			Client.instance.sendPacket(playerActionPacket);

			// This same SERVER_READY state also occurs as part of the initial join handshake, not just
			// an actual player death - translating it into a real Java ClientboundRespawnPacket during
			// that handshake resets ClientPacketListener's LevelLoadTracker back to WaitingForServer,
			// undoing LEVEL_CHUNKS_LOAD_START and leaving "Loading Terrain" stuck forever. The
			// PlayerActionType.RESPAWN acknowledgment above is still required either way to complete
			// Bedrock's handshake; only the Java-side respawn packet needs gating.
			//
			// Gating on "has StartGamePacket finished" doesn't work - this handshake's SERVER_READY
			// arrives as its own later packet, well after StartGameTranslator returns, so a flag set at
			// the end of translate() is already true by the time it shows up here. Instead, gate on
			// whether we've ever seen SERVER_READY before: the very first occurrence is always the join
			// handshake and gets swallowed (while still flipping the flag so it's not swallowed again),
			// every occurrence after that is a real respawn.
			if (Client.instance.initialSpawnComplete) {
				//TODO: correct these values, so like it's not just overworld, and survival
				Holder<DimensionType> dimensionType = DimensionTranslator.bedrockToJavaDimensionType(0);
				ResourceKey<Level> dimensionId = Level.OVERWORLD;
				CommonPlayerSpawnInfo commonPlayerSpawnInfo = new CommonPlayerSpawnInfo(dimensionType, dimensionId, -1, GameType.SURVIVAL, GameType.SURVIVAL, false, false, Optional.empty(), 0, 63);
				ClientboundRespawnPacket clientboundRespawnPacket = new ClientboundRespawnPacket(commonPlayerSpawnInfo, ClientboundRespawnPacket.KEEP_ALL_DATA);
				Client.instance.javaConnection.processServerToClientPacket(clientboundRespawnPacket);
			} else {
				Client.instance.initialSpawnComplete = true;
			}
		}

	}

	@Override
	public Class<?> getPacketClass() {
		return RespawnPacket.class;
	}

}
