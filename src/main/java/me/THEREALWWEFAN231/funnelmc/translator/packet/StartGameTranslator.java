package me.THEREALWWEFAN231.funnelmc.translator.packet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.packet.RequestChunkRadiusPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.dimension.DimensionTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.gamemode.GameModeTranslator;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

public class StartGameTranslator extends PacketTranslator<StartGamePacket> {

	private static final Logger logger = LogManager.getLogger(StartGameTranslator.class);

	public static int lastRunTimeId;//TODO: remove this, or at least move to some class accessible from the Client class, thinking of setting the player id to this, but not sure about that yet
	public static GameType DEFAULT_GAME_TYPE;

	@Override
	public void translate(StartGamePacket packet) {
		logger.warn("StartGamePacket received, dimension={} playerPos={}", packet.getDimensionId(), packet.getPlayerPosition());

		// itemDefinitions/blockDefinitions are set synchronously in ClientBatchHandler, not here -
		// see its comment for why this deferred-to-main-thread translate() is too late for that.
		Client.instance.movementMode = packet.getAuthoritativeMovementMode();
		logger.warn("[MovementDiag] AuthoritativeMovementMode={}", Client.instance.movementMode);

		int playerEntityId = (int) packet.getRuntimeEntityId();//not sure if we are suppose to use runtime id or unique id
		lastRunTimeId = playerEntityId;

		DEFAULT_GAME_TYPE = packet.getLevelGameType();
		net.minecraft.world.level.GameType gameMode = GameModeTranslator.bedrockToJava(packet.getPlayerGameType(), packet.getLevelGameType());
		net.minecraft.world.level.GameType previousGameMode = null;
		long sha256Seed = packet.getSeed();
		boolean hardcore = false;
		Set<ResourceKey<Level>> dimensionIds = new HashSet<>();//im not quite sure if it needs to be linked, it would appear not as ClientPacketListener shuffles them
		dimensionIds.add(Level.NETHER);
		dimensionIds.add(Level.OVERWORLD);
		dimensionIds.add(Level.END);
		ResourceKey<Level> dimensionId = DimensionTranslator.bedrockToJavaRegistryKey(packet.getDimensionId());
		Holder<DimensionType> dimensionType = DimensionTranslator.bedrockToJavaDimensionType(packet.getDimensionId());
		int maxPlayers = 999;
		int chunkLoadDistance = 3;
		int simulationDistance = chunkLoadDistance;
		boolean reducedDebugInfo = false;
		boolean showDeathScreen = true;
		boolean doLimitedCrafting = false;
		boolean debugWorld = false;
		boolean flatWorld = false;

		for (GameRuleData<?> gamerule : packet.getGamerules()) {
			if ("doimmediaterespawn".equals(gamerule.getName())) {
				showDeathScreen = !((Boolean) gamerule.getValue());
				break;
			}
		}

		CommonPlayerSpawnInfo commonPlayerSpawnInfo = new CommonPlayerSpawnInfo(dimensionType, dimensionId, sha256Seed, gameMode, previousGameMode, debugWorld, flatWorld, Optional.empty(), 0, 63);
		ClientboundLoginPacket clientboundLoginPacket = new ClientboundLoginPacket(playerEntityId, hardcore, dimensionIds, maxPlayers, chunkLoadDistance, simulationDistance, reducedDebugInfo, showDeathScreen, doLimitedCrafting, commonPlayerSpawnInfo, false, false);
		Client.instance.javaConnection.processServerToClientPacket(clientboundLoginPacket);

		Client.instance.onPlayerInitialized();

		// Real Java servers send this the moment they start streaming chunks - ClientPacketListener's
		// LevelLoadTracker starts in a WaitingForServer state with no timeout at all, and only moves to
		// a state with one (WaitingForPlayerChunk, 30s) once this arrives. Without it the "Loading
		// terrain" screen never dismisses no matter how many chunks actually load, since nothing ever
		// advances the tracker past its initial state.
		Client.instance.javaConnection.processServerToClientPacket(
				new ClientboundGameEventPacket(ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START, 0));
		logger.warn("Sent LEVEL_CHUNKS_LOAD_START");
		me.THEREALWWEFAN231.funnelmc.debug.LoadTrackerDiagnostics.logState("immediately-after-send");

		//TODO send a complete tag sync packet - that way water can work

		net.minecraft.client.Minecraft.getInstance().execute(() -> GameRulesChangedTranslator.onGameRulesChanged(packet.getGamerules()));

		float x = packet.getPlayerPosition().getX();
		// Bedrock's player position is eye-height-based, but Java's is feet-based - every other
		// player-position translation in this codebase accounts for this (MovePlayerPacketTranslator
		// subtracts it going in, PlayerAuthInputSender adds it going out), but this initial spawn
		// position sent the raw Bedrock Y straight through, spawning the player ~1.62 blocks above
		// where they should be. Physics/corrections would then keep pulling them back down against
		// that too-high baseline, which is what "rubberbanding in the sky" actually was.
		float y = packet.getPlayerPosition().getY() - FunnelMC.mc.player.getEyeHeight(net.minecraft.world.entity.Pose.STANDING);
		float z = packet.getPlayerPosition().getZ();
		float yaw = packet.getRotation().getX();
		float pitch = packet.getRotation().getY();
		PositionMoveRotation positionMoveRotation = new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, yaw, pitch);
		ClientboundPlayerPositionPacket clientboundPlayerPositionPacket = new ClientboundPlayerPositionPacket(0, positionMoveRotation, Collections.<Relative>emptySet());
		Client.instance.javaConnection.processServerToClientPacket(clientboundPlayerPositionPacket);
		logger.warn("Sent player position x={} y={} z={}", x, y, z);

		int chunkX = Mth.floor(x) >> 4;
		int chunkZ = Mth.floor(z) >> 4;
		ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket = new ClientboundSetChunkCacheCenterPacket(chunkX, chunkZ);
		Client.instance.javaConnection.processServerToClientPacket(clientboundSetChunkCacheCenterPacket);
		logger.warn("Sent chunk cache center chunkX={} chunkZ={}", chunkX, chunkZ);

		// Boilerplate initialization stuff
		RequestChunkRadiusPacket requestChunkRadiusPacket = new RequestChunkRadiusPacket();
		requestChunkRadiusPacket.setRadius(FunnelMC.mc.options.renderDistance().get());
		Client.instance.sendPacketImmediately(requestChunkRadiusPacket);

		SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
		setLocalPlayerAsInitializedPacket.setRuntimeEntityId(lastRunTimeId);
		Client.instance.sendPacketImmediately(setLocalPlayerAsInitializedPacket);

		// initialSpawnComplete is NOT set here - the join handshake's RespawnPacket(SERVER_READY)
		// arrives as its own later packet, after this method returns, so setting the flag this early
		// would already be true by the time it shows up and defeat the gate entirely. See
		// RespawnPacketTranslator, which owns setting this flag on the first SERVER_READY it sees.
	}

	@Override
	public Class<?> getPacketClass() {
		return StartGamePacket.class;
	}

}
