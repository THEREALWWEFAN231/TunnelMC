package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtMapBuilder;
import com.nukkitx.nbt.util.stream.NetworkDataInputStream;
import com.nukkitx.network.VarInts;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.tunnelmc.nukkit.BitArray;
import me.THEREALWWEFAN231.tunnelmc.nukkit.BitArrayVersion;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.LegacyBlockPaletteManager;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.collection.IndexedIterable;
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
import java.util.List;

public class LevelChunkTranslator extends PacketTranslator<LevelChunkPacket> {
	private static final IndexedIterable<Biome> BIOME_REGISTRY = DynamicRegistryManager.create().get(Registry.BIOME_KEY);

	private final List<LevelChunkPacket> chunksOutOfRenderDistance = new ArrayList<>();

	public LevelChunkTranslator() {
		EventManager.register(this);
	}

	//TODO: block entities, biomes, and probably lighting

	@Override
	public void translate(LevelChunkPacket packet) {
		/*
		 * So id like to make a note on this, so i dont forget how this works, and or so someone else knows how this works
		 * a chunk is made up of 16 vertical sections, 16 blocks tall each, meaning 16(blocks) * 16(vertical sections) = 256(the build height)
		 * subChunksLength is equal the highest section(remember there is only 16 sections), so if the chunk only has a few blocks that are
		 * y <= 15(maybe 16?) subChunksLength will equal 1, because only 1 section is being used. another example would be, say the chunk is
		 * completely empty, except there is 1 stone block at y = 50, there will be 4 sections, 16*3=48(not big enough), 16*4=64(big enough, but not too big)
		 * 
		 * the data bytes in the packet hold all the chunk information, first we have to iterate though all the sections(explained above)
		 * we then read a byte(im not fully sure what its used for, yet, but geyser names it "CHUNK_SECTION_VERSION"), then we read another byte
		 * "storageSize" which appears to always equal 2, from looking at nukkit and stuff, it appears the server sends 2 chunk layers? im not even fully sure
		 * what that means, but it appears the second one is always empty. we then iterate though all the "storages"(like i said always 2?)
		 * we then read a byte which is the "paletteHeader"? from that we get the "paletteVersion" from that we can create a "BitArray"
		 * which is like hashed and or compressed data?(im not even fully sure). from the bit array we created, we can then get the chunk sections' "words"
		 * by iterating though the max "word" count in the BitArray we created and reading a 32 bit integer which has compressed data? basically im pretty sure
		 * the "words" in a BitArray are compressed, so if input the 32 bit integer we read into the BitArrays' "words" we can use the BitArray to get the real
		 * uncompressed data(ill explain what these "words" are in a minute). after we read all the words we then read a VarInt which has the size of the block palette
		 * 
		 * What is a block palette? each section in a chunk(remember theres a max of 16), has a block palette which contains data about the blocks in the section
		 * for example, index0=air, index1=stone, index2=diamond_ore, that is our block palette, these are the only blocks used in this section of a chunk.
		 * thats an example, the block palettes' values(air, stone, diamond_ore) aren't actual strings they are integers, the blocks mcbe "runtimeId".
		 * 
		 * So after we get the size of the block palette(x) we then read x VarInts to get the palette information, so now we have an int array which are be referred
		 * to by index to get the runtime id of any block in the section. We then have to do that, we iterate though the x, then z, then y coordinate of the chunk section
		 * we use the BitArray(which has "words") the get the "decompressed" block palette index, once we have this, we refer to the block palette, and get the mcbe runtimeId
		 * or the specific xyz coordinate, which we then translate to java.
		 * 
		 * I know this is probably confusing, the code is probably more clear then this, but i tried.
		 * 
		 * TLDR: we read some information about a chunk section, which has a key and value, the value being a mcbe block runtimeId, we go though all block positions
		 * for the chunks' section(that we are currently iterating). from that we get the key, which of course gives us the value(mcbe block runtimeId)
		 * 
		 */

		int chunkX = packet.getChunkX();
		int chunkZ = packet.getChunkZ();

		if (TunnelMC.mc.player != null) {
			if (!this.isChunkInRenderDistance(chunkX, chunkZ)) {
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
				manage0VersionChunk(byteBuf, chunkSections[sectionIndex]);
				continue;
			}

			byte storageSize = chunkVersion == 1 ? 1 : byteBuf.readByte();

			for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
				byte paletteHeader = byteBuf.readByte();
				boolean isRuntime = (paletteHeader & 1) == 1;
				int paletteVersion = (paletteHeader | 1) >> 1;

				BitArrayVersion bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

				int maxBlocksInSection = 4096; // 16*16*16
				BitArray bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
				int wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);

				for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
					int word = byteBuf.readIntLE();
					bitArray.getWords()[wordIterationIndex] = word;
				}

				int paletteSize = VarInts.readInt(byteBuf);
				int[] sectionPalette = new int[paletteSize];
				NBTInputStream nbtStream = isRuntime ? null : new NBTInputStream(new NetworkDataInputStream(new ByteBufInputStream(byteBuf)));
				for (int i = 0; i < paletteSize; i++) {
					if (isRuntime) {
						sectionPalette[i] = VarInts.readInt(byteBuf);
					} else {
						try {
							NbtMapBuilder map = ((NbtMap) nbtStream.readTag()).toBuilder();
							// For some reason, persistent chunks don't include the "minecraft:" that should be used in state names.
							map.replace("name", "minecraft:" + map.get("name").toString());
							sectionPalette[i] = BlockPaletteTranslator.getBedrockBlockId(BlockPaletteTranslator.bedrockStateFromNBTMap(map.build()));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if (storageReadIndex == 0) {
					int index = 0;
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							for (int y = 0; y < 16; y++) {
								int paletteIndex = bitArray.get(index);
								int mcbeBlockId = sectionPalette[paletteIndex];
								if (mcbeBlockId != BlockPaletteTranslator.AIR_BEDROCK_BLOCK_ID) {

									BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(mcbeBlockId);

									chunkSections[sectionIndex].setBlockState(x, y, z, blockState);
								}
								index++;
							}
						}
					}
				}
			}
		}

		byte[] bedrockBiomes = new byte[256];
		byteBuf.readBytes(bedrockBiomes);

		int[] javaBiomes = new int[1024];
		int javaBiomeCount = 0;
		for (byte bedrockBiome : bedrockBiomes) {
			byte desiredBiome = bedrockBiome;
			if (BIOME_REGISTRY.get(desiredBiome) == null) {
				// Invalid biome that spams the console
				// I got -98 so it could also be an encoding issue
				desiredBiome = 1;
			}
			javaBiomes[javaBiomeCount++] = desiredBiome;
			// convert 256 to 1024
			javaBiomes[javaBiomeCount++] = desiredBiome;
			javaBiomes[javaBiomeCount++] = desiredBiome;
			javaBiomes[javaBiomeCount++] = desiredBiome;
		}

		BiomeArray biomeArray = new BiomeArray(BIOME_REGISTRY, javaBiomes);
		WorldChunk worldChunk = new WorldChunk(null, new ChunkPos(chunkX, chunkZ), biomeArray);

		for (int i = 0; i < worldChunk.getSectionArray().length; i++) {
			worldChunk.getSectionArray()[i] = chunkSections[i];
		}

		ChunkDataS2CPacket chunkDeltaUpdateS2CPacket = new ChunkDataS2CPacket(worldChunk, 65535);
		Client.instance.javaConnection.processServerToClientPacket(chunkDeltaUpdateS2CPacket);
	}

	/**
	 * Used for PocketMine.
	 */
	private void manage0VersionChunk(ByteBuf byteBuf, ChunkSection chunkSection) {
		byte[] blockIds = new byte[4096];
		byteBuf.readBytes(blockIds);

		byte[] metaIds = new byte[2048];
		byteBuf.readBytes(metaIds);

		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					int idx = (x << 8) + (z << 4) + y;
					int id = blockIds[idx];
					int meta = metaIds[idx >> 1] >> (idx & 1) * 4 & 15;

					BlockState blockState = LegacyBlockPaletteManager.LEGACY_BLOCK_TO_JAVA_ID.get(id << 6 | meta);

					if (blockState != null) {
						chunkSection.setBlockState(x, y, z, blockState);
					}
				}
			}
		}
	}

	@Override
	public Class<?> getPacketClass() {
		return LevelChunkPacket.class;
	}

	public boolean isChunkInRenderDistance(int chunkX, int chunkZ) {
		int playerChunkX = MathHelper.floor(TunnelMC.mc.player.getX()) >> 4;
		int playerChunkZ = MathHelper.floor(TunnelMC.mc.player.getZ()) >> 4;
		return Math.abs(chunkX - playerChunkX) <= TunnelMC.mc.options.viewDistance && Math.abs(chunkZ - playerChunkZ) <= TunnelMC.mc.options.viewDistance;
	}

	@EventTarget
	public void onEvent(EventPlayerTick event) {
		// this needs some work, general chunk loading needs some work as well
		for(int i = 0; i < this.chunksOutOfRenderDistance.size(); i++) { // could use an iterator, but that's no fun?
			LevelChunkPacket levelChunkPacket = this.chunksOutOfRenderDistance.get(i);
			if (!this.isChunkInRenderDistance(levelChunkPacket.getChunkX(), levelChunkPacket.getChunkZ())) {
				continue;
			}

			this.translate(levelChunkPacket);
			this.chunksOutOfRenderDistance.remove(i);
			i--;
		}
	}

}
