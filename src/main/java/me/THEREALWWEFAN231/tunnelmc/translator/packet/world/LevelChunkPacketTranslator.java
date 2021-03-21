package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import java.util.ArrayList;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.nukkitx.network.VarInts;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.tunnelmc.nukkit.BitArray;
import me.THEREALWWEFAN231.tunnelmc.nukkit.BitArrayVersion;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
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

public class LevelChunkPacketTranslator extends PacketTranslator<LevelChunkPacket> {
	private static final IndexedIterable<Biome> BIOME_REGISTRY = DynamicRegistryManager.create().get(Registry.BIOME_KEY);

	private final ArrayList<LevelChunkPacket> chunksOutOfRenderDistance = new ArrayList<>();

	public LevelChunkPacketTranslator() {
		EventManager.register(this);
	}

	//TODO: block entities, biomes, and probably lighting

	@SuppressWarnings("unused")
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

		//I like setting it in PlayerMoveC2SPacketTranslator, better this this
		/*if (TunnelMC.mc.player != null) {
			//make sure far away chunks load
			ChunkRenderDistanceCenterS2CPacket renderDistanceCenterPacket = new ChunkRenderDistanceCenterS2CPacket(MathHelper.floor(TunnelMC.mc.player.getX()) >> 4, MathHelper.floor(TunnelMC.mc.player.getZ()) >> 4);
			Client.instance.javaConnection.processServerToClientPacket(renderDistanceCenterPacket);
		}*/

		ChunkSection[] chunkSections = new ChunkSection[16];

		int chunkX = packet.getChunkX();
		int chunkZ = packet.getChunkZ();

		if (TunnelMC.mc.player != null) {
			if (!this.isChunkInRenderDistance(chunkX, chunkZ)) {
				this.chunksOutOfRenderDistance.add(packet);
				return;
			}
		}

		ByteBuf byteBuf = Unpooled.buffer();
		byteBuf.writeBytes(packet.getData());

		for (int sectionIndex = 0; sectionIndex < packet.getSubChunksLength(); sectionIndex++) {
			chunkSections[sectionIndex] = new ChunkSection(sectionIndex);
			byteBuf.readByte(); // geyser says CHUNK_SECTION_VERSION
			byte storageSize = byteBuf.readByte();

			for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
				// PalettedBlockStorage
				byte paletteHeader = byteBuf.readByte();
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
				int[] sectionPalette = new int[paletteSize]; // so this holds all the different block types in the chunk section, first index is always air, then we have the block ids
				for (int i = 0; i < paletteSize; i++) {
					int id = VarInts.readInt(byteBuf);

					sectionPalette[i] = id;
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
				/*
				else if (storageReadIndex == 1) {
					int index = 0;
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							for (int y = 0; y < 16; y++) {
								if (sectionPalette[0] != 134 || sectionPalette.length > 1) {
									System.out.println(Arrays.toString(sectionPalette));
								}
								int paletteIndex = bitArray.get(index);
								if (paletteIndex != 0) {
									System.out.println(paletteIndex);
								}
								index++;
							}
						}
					}
				}*/
			}

			//break;
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
