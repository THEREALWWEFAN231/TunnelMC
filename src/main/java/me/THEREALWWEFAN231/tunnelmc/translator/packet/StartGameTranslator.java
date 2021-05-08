package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.nukkitx.protocol.bedrock.data.GameRuleData;
import com.nukkitx.protocol.bedrock.data.GameType;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;

import com.nukkitx.protocol.bedrock.packet.TickSyncPacket;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.dimension.DimensionTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.gamemode.GameModeTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class StartGameTranslator extends PacketTranslator<StartGamePacket> {
	
	public static int lastRunTimeId;//TODO: remove this, or at least move to some class accessible from the Client class, thinking of setting the player id to this, but not sure about that yet
	public static GameType DEFAULT_GAME_TYPE;

	@Override
	public void translate(StartGamePacket packet) {
		int playerEntityId = (int) packet.getRuntimeEntityId();//not sure if we are suppose to use runtime id or unique id
		lastRunTimeId = playerEntityId;

		for (StartGamePacket.ItemEntry itemEntry : packet.getItemEntries()) {
			if (itemEntry.getIdentifier().equals("minecraft:shield")) {
				Client.instance.bedrockClient.getSession().getHardcodedBlockingId().set(itemEntry.getId());
				break;
			}
		}

		DEFAULT_GAME_TYPE = packet.getLevelGameType();
		GameMode gameMode = GameModeTranslator.bedrockToJava(packet.getPlayerGameType(), packet.getLevelGameType());
		GameMode previousGameMode = GameMode.NOT_SET;
		long sha256Seed = packet.getSeed();
		boolean hardcore = false;
		Set<RegistryKey<World>> dimensionIds = new HashSet<>();//im not quite sure if it needs to be linked, it would appear not as ClientPlayNetworkHandler shuffles them
		dimensionIds.add(World.NETHER);
		dimensionIds.add(World.OVERWORLD);
		dimensionIds.add(World.END);
		DynamicRegistryManager.Impl registryManager = DynamicRegistryManager.create();
		DimensionType dimensionType = DimensionTranslator.bedrockToJava(packet.getDimensionId());
		RegistryKey<World> dimensionId = DimensionTranslator.bedrockToJavaRegistryKey(packet.getDimensionId());
		int maxPlayers = 999;
		int chunkLoadDistance = 3;
		boolean reducedDebugInfo = false;
		boolean showDeathScreen = true;
		boolean debugWorld = false;
		boolean flatWorld = false;

		for (GameRuleData<?> gamerule : packet.getGamerules()) {
			if ("doimmediaterespawn".equals(gamerule.getName())) {
				showDeathScreen = !((Boolean) gamerule.getValue());
				break;
			}
		}

		GameJoinS2CPacket gameJoinS2CPacket = new GameJoinS2CPacket(playerEntityId, gameMode, previousGameMode, sha256Seed,
				hardcore, dimensionIds, registryManager, dimensionType, dimensionId, maxPlayers, chunkLoadDistance,
				reducedDebugInfo, showDeathScreen, debugWorld, flatWorld);
		Client.instance.javaConnection.processServerToClientPacket(gameJoinS2CPacket);

		//TODO send a complete SynchronizeTagsS2CPacket - that way water can work

		MinecraftClient.getInstance().execute(() -> GameRulesChangedTranslator.onGameRulesChanged(packet.getGamerules()));

		float x = packet.getPlayerPosition().getX();
		float y = packet.getPlayerPosition().getY();
		float z = packet.getPlayerPosition().getZ();
		float yaw = packet.getRotation().getX();
		float pitch = packet.getRotation().getY();
		int teleportId = 0;
		PlayerPositionLookS2CPacket playerPositionLookS2CPacket = new PlayerPositionLookS2CPacket(x, y, z, yaw, pitch, Collections.emptySet(), teleportId);
		Client.instance.javaConnection.processServerToClientPacket(playerPositionLookS2CPacket);

		int chunkX = MathHelper.floor(x) >> 4;
		int chunkZ = MathHelper.floor(z) >> 4;
		ChunkRenderDistanceCenterS2CPacket chunkRenderDistanceCenterS2CPacket = new ChunkRenderDistanceCenterS2CPacket(chunkX, chunkZ);
		Client.instance.javaConnection.processServerToClientPacket(chunkRenderDistanceCenterS2CPacket);

		// Boilerplate initialization stuff
		RequestChunkRadiusPacket requestChunkRadiusPacket = new RequestChunkRadiusPacket();
		requestChunkRadiusPacket.setRadius(TunnelMC.mc.options.viewDistance);
		Client.instance.sendPacketImmediately(requestChunkRadiusPacket);

		Client.instance.sendPacketImmediately(new TickSyncPacket());

		SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
		setLocalPlayerAsInitializedPacket.setRuntimeEntityId(lastRunTimeId);
		Client.instance.sendPacketImmediately(setLocalPlayerAsInitializedPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return StartGamePacket.class;
	}

}
