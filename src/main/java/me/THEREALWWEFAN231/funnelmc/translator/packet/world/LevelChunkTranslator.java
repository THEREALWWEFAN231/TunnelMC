package me.THEREALWWEFAN231.funnelmc.translator.packet.world;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.util.stream.NetworkDataInputStream;
import org.cloudburstmc.protocol.common.util.VarInts;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.funnelmc.nukkit.BitArray;
import me.THEREALWWEFAN231.funnelmc.nukkit.BitArrayVersion;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.LegacyBlockPaletteManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class LevelChunkTranslator extends PacketTranslator<LevelChunkPacket> {

	private final Logger logger = LogManager.getLogger(LevelChunkTranslator.class);

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

		if (FunnelMC.mc.player != null) {
			if (!this.isChunkInRenderDistance(chunkX, chunkZ)) {
				this.chunksOutOfRenderDistance.add(packet);
				return;
			}
		}

		// Java's section array for a modern world doesn't start at Y=0 - a -64..320 world has
		// getMinSectionY()=-4, so section index 0 holds Y=-64..-49. Bedrock's legacy subchunk
		// numbering (versions 1/8) is still just "the Nth subchunk in this packet is bedrock
		// section Y=N", implicitly assuming a world that starts at Y=0 - so every bedrock section
		// has to be placed at (bedrockSectionY - minSectionY) in Java's array, not at its raw
		// packet-order index. Version 9 (the extended-height format) sends an explicit signed
		// section Y instead of relying on packet order at all, for the same reason: sections
		// aren't guaranteed contiguous from 0 in a world with negative height.
		int minSectionY = FunnelMC.mc.level.getMinSectionY();
		LevelChunkSection[] chunkSections = new LevelChunkSection[FunnelMC.mc.level.getSectionsCount()];

		ByteBuf byteBuf = Unpooled.buffer();
		// ClientBatchHandler retains this before deferring us onto the main thread (see its comment) -
		// release our hold once we've copied what we need out of it.
		byteBuf.writeBytes(packet.getData());
		packet.getData().release();

		for (int sectionIndex = 0; sectionIndex < packet.getSubChunksLength(); sectionIndex++) {
			int chunkVersion = byteBuf.readByte();

			// Bedrock section Y defaults to the subchunk's position in the packet (versions 1/8,
			// which predate negative height and always start at Y=0) - version 9 below overrides
			// this with the real signed value it reads off the wire.
			int sectionY = sectionIndex;

			if (chunkVersion != 1 && chunkVersion != 8 && chunkVersion != 9) {
				int javaSectionIndex = sectionY - minSectionY;
				LevelChunkSection legacySection = new LevelChunkSection(FunnelMC.mc.level.palettedContainerFactory());
				manage0VersionChunk(byteBuf, legacySection);
				if (javaSectionIndex >= 0 && javaSectionIndex < chunkSections.length) {
					chunkSections[javaSectionIndex] = legacySection;
				}
				continue;
			}

			byte storageSize = chunkVersion == 1 ? 1 : byteBuf.readByte();

			// Version 9 is the extended-height subchunk format (introduced alongside the -64..320
			// world height range) - unlike version 8, sections aren't guaranteed contiguous from Y=0
			// anymore, so the server appends a signed Y index byte AFTER the storage count, before
			// the same storage-layer data version 8 uses. The Y index was previously read in the
			// wrong position (right after the version byte, before storage count), which fed the
			// Y index into storageSize and vice versa - a small/negative "storageSize" silently
			// skipped the storage-layer loop entirely, leaving that section's real data unconsumed
			// and corrupting the read cursor for every section after it in the same packet.
			if (chunkVersion == 9) {
				sectionY = byteBuf.readByte();
			}

			int javaSectionIndex = sectionY - minSectionY;
			LevelChunkSection section = new LevelChunkSection(FunnelMC.mc.level.palettedContainerFactory());
			if (javaSectionIndex >= 0 && javaSectionIndex < chunkSections.length) {
				chunkSections[javaSectionIndex] = section;
			}

			for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
				// paletteHeader must be treated as unsigned here - readByte() gives a signed byte,
				// and >> on a byte widened to int is an arithmetic (sign-preserving) shift, so any
				// header with bit 7 set previously produced a negative paletteVersion instead of the
				// real bits-per-entry value, which BitArrayVersion.get() then rejected (or worse,
				// matched nothing/threw), leaving the rest of this section's reads misaligned.
				int paletteHeader = byteBuf.readByte() & 0xFF;
				boolean isRuntime = (paletteHeader & 1) == 1;
				int paletteVersion = paletteHeader >> 1;

				// bits-per-block of 0 is Bedrock's "single value" encoding: every block in this
				// storage layer is the same one palette entry, so there's no bit array of indices
				// on the wire at all - just the palette itself (always exactly one entry). Treating
				// this as a normal BitArrayVersion was throwing "Invalid palette version: 0" and,
				// worse, leaving the palette bytes unread and every subsequent read in the packet
				// misaligned.
				if (paletteVersion == 0) {
					int[] singleValuePalette = readPalette(byteBuf, isRuntime, 1);
					if (storageReadIndex == 0 && singleValuePalette[0] != BlockPaletteTranslator.AIR_BEDROCK_BLOCK_ID) {
						BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(singleValuePalette[0]);
						if (blockState != null) {
							for (int x = 0; x < 16; x++) {
								for (int z = 0; z < 16; z++) {
									for (int y = 0; y < 16; y++) {
										section.setBlockState(x, y, z, blockState);
									}
								}
							}
						}
					}
					continue;
				}

				BitArrayVersion bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

				int maxBlocksInSection = 4096; // 16*16*16
				BitArray bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
				int wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);

				for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
					int word = byteBuf.readIntLE();
					bitArray.getWords()[wordIterationIndex] = word;
				}

				int paletteSize = VarInts.readInt(byteBuf);
				int[] sectionPalette = readPalette(byteBuf, isRuntime, paletteSize);

				if (storageReadIndex == 0) {
					int index = 0;
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							for (int y = 0; y < 16; y++) {
								int paletteIndex = bitArray.get(index);
								int mcbeBlockId = sectionPalette[paletteIndex];
								if (mcbeBlockId != BlockPaletteTranslator.AIR_BEDROCK_BLOCK_ID) {

									BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(mcbeBlockId);

									// Bedrock block runtime IDs the palette translator hasn't mapped to a Java
									// BlockState (e.g. blocks with no Java equivalent yet) come back null here -
									// setBlockState() requires a non-null state, so leave the position as its
									// section default (air) instead of crashing the whole chunk translation.
									if (blockState != null) {
										section.setBlockState(x, y, z, blockState);
									}
								}
								index++;
							}
						}
					}
				}
			}
		}

		// TODO: biomes are no longer a flat per-chunk array in modern Minecraft (each LevelChunkSection
		// carries its own PalettedContainerRO<Holder<Biome>>); for now we just consume the rest of the
		// buffer and leave every section on its default biome, same as the pre-existing "TODO: biomes"
		// gap. This is a per-subchunk palette in the modern Bedrock protocol, not a fixed-size flat
		// array, so its length varies (and can be shorter than the old legacy 256-byte assumption,
		// which threw IndexOutOfBoundsException on chunks with little/no remaining data).
		byteBuf.skipBytes(byteBuf.readableBytes());

		LevelChunk worldChunk = new LevelChunk(FunnelMC.mc.level, new ChunkPos(chunkX, chunkZ));

		// chunkSections is already sized and indexed to match worldChunk's real height-shifted
		// section array (see the javaSectionIndex computation above), so this is now a direct
		// overlay rather than an assumption that both arrays start at the same Y.
		LevelChunkSection[] sections = worldChunk.getSections();
		for (int i = 0; i < sections.length && i < chunkSections.length; i++) {
			if (chunkSections[i] != null) {
				sections[i] = chunkSections[i];
			}
		}

		ClientboundLevelChunkWithLightPacket chunkDeltaUpdateS2CPacket = new ClientboundLevelChunkWithLightPacket(
				worldChunk, FunnelMC.mc.level.getLightEngine(), new BitSet(), new BitSet());
		Client.instance.javaConnection.processServerToClientPacket(chunkDeltaUpdateS2CPacket);

		this.logger.warn("Translated+sent chunk ({}, {}): bedrock subChunksLength={}, java worldChunk sections={} (level minY={}, height={})",
				chunkX, chunkZ, packet.getSubChunksLength(), sections.length,
				FunnelMC.mc.level.getMinY(), FunnelMC.mc.level.getHeight());
	}

	private int[] readPalette(ByteBuf byteBuf, boolean isRuntime, int paletteSize) {
		int[] palette = new int[paletteSize];
		NBTInputStream nbtStream = isRuntime ? null : new NBTInputStream(new NetworkDataInputStream(new ByteBufInputStream(byteBuf)));
		for (int i = 0; i < paletteSize; i++) {
			if (isRuntime) {
				palette[i] = VarInts.readInt(byteBuf);
			} else {
				try {
					NbtMapBuilder map = ((NbtMap) nbtStream.readTag()).toBuilder();
					// For some reason, persistent chunks don't include the "minecraft:" that should be used in state names.
					map.replace("name", "minecraft:" + map.get("name").toString());
					palette[i] = BlockPaletteTranslator.getBedrockBlockId(BlockPaletteTranslator.bedrockStateFromNBTMap(map.build()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return palette;
	}

	/**
	 * Used for PocketMine.
	 */
	private void manage0VersionChunk(ByteBuf byteBuf, LevelChunkSection chunkSection) {
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
		int playerChunkX = Mth.floor(FunnelMC.mc.player.getX()) >> 4;
		int playerChunkZ = Mth.floor(FunnelMC.mc.player.getZ()) >> 4;
		int viewDistance = FunnelMC.mc.options.getEffectiveRenderDistance();
		return Math.abs(chunkX - playerChunkX) <= viewDistance && Math.abs(chunkZ - playerChunkZ) <= viewDistance;
	}

	@EventTarget
	public void onEvent(EventPlayerTick event) {
		// this needs some work, general chunk loading needs some work as well
		for(int i = 0; i < this.chunksOutOfRenderDistance.size(); i++) { // could use an iterator, but that's no fun?
			LevelChunkPacket levelChunkPacket = this.chunksOutOfRenderDistance.get(i);
			if (!this.isChunkInRenderDistance(levelChunkPacket.getChunkX(), levelChunkPacket.getChunkZ())) {
				continue;
			}

			try {
				this.translate(levelChunkPacket);
			} catch (Exception e) {
				// A chunk that throws here would otherwise stay in this list forever and get
				// retried (and rethrow) every single tick, flooding the log with duplicates of the
				// same failure instead of surfacing it once. Drop it either way - retrying a chunk
				// whose translate() already failed isn't going to make it succeed next tick.
				this.logger.error("Failed to translate out-of-render-distance chunk ({}, {})",
						levelChunkPacket.getChunkX(), levelChunkPacket.getChunkZ(), e);
			}
			this.chunksOutOfRenderDistance.remove(i);
			i--;
		}
	}

}
