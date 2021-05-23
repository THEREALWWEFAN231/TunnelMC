package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.nukkitx.protocol.bedrock.data.GameRuleData;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StartGameTranslator extends PacketTranslator<StartGamePacket> {

	@Override
	public void translate(StartGamePacket packet) {
		for (StartGamePacket.ItemEntry itemEntry : packet.getItemEntries()) {
			if (itemEntry.getIdentifier().equals("minecraft:shield")) {
				Client.instance.bedrockClient.getSession().getHardcodedBlockingId().set(itemEntry.getId());
				break;
			}
		}

		Client.instance.entityRuntimeId = (int) packet.getRuntimeEntityId();
		Client.instance.defaultGameMode = packet.getLevelGameType();

		GameMode gameMode = GameModeTranslator.bedrockToJava(packet.getPlayerGameType(), packet.getLevelGameType());
		GameMode previousGameMode = GameMode.NOT_SET;
		Set<RegistryKey<World>> dimensionIds = new HashSet<>();
		dimensionIds.add(World.NETHER);
		dimensionIds.add(World.OVERWORLD);
		dimensionIds.add(World.END);
		DynamicRegistryManager.Impl registryManager = DynamicRegistryManager.create();
		DimensionType dimensionType = DimensionTranslator.bedrockToJava(packet.getDimensionId());
		RegistryKey<World> dimensionId = DimensionTranslator.bedrockToJavaRegistryKey(packet.getDimensionId());

		int seed = packet.getSeed();
		int maxPlayers = 999;
		int chunkLoadDistance = 3;
		boolean showDeathScreen = true;

		for (GameRuleData<?> gameRule : packet.getGamerules()) {
			if (gameRule.getName().equals("doimmediaterespawn")) {
				showDeathScreen = !((Boolean) gameRule.getValue());
				break;
			}
		}

		GameJoinS2CPacket gameJoinS2CPacket = new GameJoinS2CPacket(Client.instance.entityRuntimeId, gameMode, previousGameMode, seed, false, dimensionIds, registryManager, dimensionType, dimensionId, maxPlayers, chunkLoadDistance, false, showDeathScreen, false, false);
		Client.instance.javaConnection.processServerToClientPacket(gameJoinS2CPacket);
		
		Client.instance.onPlayerInitialized();

		// TODO: Send a complete SynchronizeTagsS2CPacket so that water can work.

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

		RequestChunkRadiusPacket requestChunkRadiusPacket = new RequestChunkRadiusPacket();
		requestChunkRadiusPacket.setRadius(TunnelMC.mc.options.viewDistance);
		Client.instance.sendPacketImmediately(requestChunkRadiusPacket);

		Client.instance.sendPacketImmediately(new TickSyncPacket());

		SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
		setLocalPlayerAsInitializedPacket.setRuntimeEntityId(Client.instance.entityRuntimeId);
		Client.instance.sendPacketImmediately(setLocalPlayerAsInitializedPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return StartGamePacket.class;
	}

}