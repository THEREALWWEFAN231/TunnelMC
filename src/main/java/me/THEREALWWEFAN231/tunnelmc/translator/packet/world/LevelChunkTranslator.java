package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtMapBuilder;
import com.nukkitx.nbt.util.stream.NetworkDataInputStream;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * For more information, check out the following gist:
 * https://gist.github.com/dktapps/8a4f23d2bf32ea7091ef14e4aac46170
 */
public class LevelChunkTranslator extends PacketTranslator<LevelChunkPacket> {

	private static final IndexedIterable<Biome> BIOMES_REGISTRY = DynamicRegistryManager.create().get(Registry.BIOME_KEY);

	private final List<LevelChunkPacket> chunksOutOfRenderDistance = new ArrayList<>();

	public LevelChunkTranslator() {
		EventManager.register(this);
	}

	@Override
	public void translate(LevelChunkPacket packet) {
		int chunkX = packet.getChunkX();
		int chunkZ = packet.getChunkZ();

		if (TunnelMC.mc.player != null) {
			if (this.isChunkInRenderDistance(chunkX, chunkZ)) {
				this.chunksOutOfRenderDistance.add(packet);
				return;
			}
		}

		ChunkSection[] chunkSections = new ChunkSection[16];

		ByteBuf byteBuf = Unpooled.buffer();
		byteBuf.writeBytes(packet.getData());

		for (int sectionIndex = 0; sectionIndex < packet.getSubChunksLength(); sectionIndex++) {
			chunkSections[sectionIndex] = new ChunkSection(sectionIndex);
			int chunkVersion = byteBuf.readByte();
			if (chunkVersion != 1 && chunkVersion != 8) {
				System.out.println("Decoding a version zero chunk...");
				LevelChunkDecoder.networkDecodeVersionZero(byteBuf, chunkSections[sectionIndex]);
				continue;
			}
			if (chunkVersion == 1) {
				System.out.println("Decoding a version one chunk...");
				LevelChunkDecoder.networkDecodeVersionOne(byteBuf, chunkSections[sectionIndex]);
				continue;
			}

			System.out.println("Decoding a version eight chunk...");
			LevelChunkDecoder.networkDecodeVersionEight(byteBuf, chunkSections, sectionIndex, byteBuf.readByte());
		}

		byte[] bedrockBiomes = new byte[256];
		byteBuf.readBytes(bedrockBiomes);

		// TODO: Block entities

		int[] javaBiomes = new int[1024];
		int javaBiomesCount = 0;
		for (byte bedrockBiome : bedrockBiomes) {
			byte desiredBiome = bedrockBiome;

			if (BIOMES_REGISTRY.get(desiredBiome) == null) {
				desiredBiome = 1;
			}

			javaBiomes[javaBiomesCount++] = desiredBiome;
			javaBiomes[javaBiomesCount++] = desiredBiome;
			javaBiomes[javaBiomesCount++] = desiredBiome;
			javaBiomes[javaBiomesCount++] = desiredBiome;
		}

		BiomeArray biomeArray = new BiomeArray(BIOMES_REGISTRY, javaBiomes);
		WorldChunk worldChunk = new WorldChunk(null, new ChunkPos(chunkX, chunkZ), biomeArray);

		for (int i = 0; i < worldChunk.getSectionArray().length; i++) {
			worldChunk.getSectionArray()[i] = chunkSections[i];
		}

		ChunkDataS2CPacket chunkDeltaUpdateS2CPacket = new ChunkDataS2CPacket(worldChunk, 65535);
		Client.instance.javaConnection.processServerToClientPacket(chunkDeltaUpdateS2CPacket);
	}

	@Override
	public Class<?> getPacketClass() {
		return LevelChunkPacket.class;
	}

	public boolean isChunkInRenderDistance(int chunkX, int chunkZ) {
		if (TunnelMC.mc.player == null) {
			return false;
		}
		int playerChunkX = MathHelper.floor(TunnelMC.mc.player.getX()) >> 4;
		int playerChunkZ = MathHelper.floor(TunnelMC.mc.player.getZ()) >> 4;
		return Math.abs(chunkX - playerChunkX) > TunnelMC.mc.options.viewDistance || Math.abs(chunkZ - playerChunkZ) > TunnelMC.mc.options.viewDistance;
	}

	@EventTarget
	public void onEvent(EventPlayerTick event) {
		// This needs some work, general chunk loading needs some work as well.
		Iterator<LevelChunkPacket> iterator = chunksOutOfRenderDistance.iterator();
		while (iterator.hasNext()) {
			LevelChunkPacket chunk = iterator.next();
			if (this.isChunkInRenderDistance(chunk.getChunkX(), chunk.getChunkZ())) {
				continue;
			}

			translate(chunk);
			iterator.remove();
		}
	}

}
