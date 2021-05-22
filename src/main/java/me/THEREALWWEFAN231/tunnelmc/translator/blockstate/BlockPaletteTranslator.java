package me.THEREALWWEFAN231.tunnelmc.translator.blockstate;

import com.nukkitx.nbt.NbtList;
import com.nukkitx.nbt.NbtMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Map;

/*
 * as of 1.16.100, the block palette is static between all servers, so we can load this once and be over it
 * it uses BlockStateTranslator which loaded blocks.json, from BlockStateTranslator we can match blocks and get their runtime id for a Bedrock server
 */
public class BlockPaletteTranslator {

	public static int AIR_BEDROCK_BLOCK_ID;
	public static int WATER_BEDROCK_BLOCK_ID;

	// Used for persistent v8 decoding.
	private static final Object2IntMap<String> BEDROCK_BLOCK_STATE_TO_RUNTIME_ID = new Object2IntOpenHashMap<>();

	public static final Int2ObjectMap<BlockState> RUNTIME_ID_TO_BLOCK_STATE = new Int2ObjectOpenHashMap<>();
	public static final Object2IntMap<BlockState> BLOCK_STATE_TO_RUNTIME_ID = new Object2IntOpenHashMap<>();

	public static void loadMap(NbtList<NbtMap> blockPaletteData) {
		int runtimeId = 0;
		for (NbtMap nbtMap : blockPaletteData) {
			BedrockBlockState bedrockBlockState = bedrockStateFromNBTMap(nbtMap);
			BEDROCK_BLOCK_STATE_TO_RUNTIME_ID.put(bedrockBlockState.toString(), runtimeId);

			BlockState blockState = BlockStateTranslator.BEDROCK_BLOCK_STATE_STRING_TO_JAVA_BLOCK_STATE.get(bedrockBlockState.toString());
			if (blockState != null) {
				RUNTIME_ID_TO_BLOCK_STATE.put(runtimeId, blockState);
				BLOCK_STATE_TO_RUNTIME_ID.put(blockState, runtimeId);
				if (bedrockBlockState.identifier.equals("minecraft:air")) {
					AIR_BEDROCK_BLOCK_ID = runtimeId;
				} else if (bedrockBlockState.identifier.equals("minecraft:water")) {
					WATER_BEDROCK_BLOCK_ID = runtimeId;
				}
			} else {
				System.out.println("Unable to find suitable block state for " + bedrockBlockState.toString());
				RUNTIME_ID_TO_BLOCK_STATE.put(runtimeId, Blocks.STONE.getDefaultState());//we could probably put the default state, but for now we will use stone
			}

			runtimeId++;
		}

	}

	public static int getBedrockBlockId(BedrockBlockState state) {
		return BlockPaletteTranslator.BEDROCK_BLOCK_STATE_TO_RUNTIME_ID.getOrDefault(state.toString(), AIR_BEDROCK_BLOCK_ID);
	}

	public static BedrockBlockState bedrockStateFromNBTMap(NbtMap nbtMap) {
		String mcbeStringBlockName = nbtMap.getString("name");
		NbtMap blockStates = nbtMap.getCompound("states");

		BedrockBlockState bedrockBlockState = new BedrockBlockState();
		bedrockBlockState.identifier = mcbeStringBlockName;

		for (Map.Entry<String, Object> blockState : blockStates.entrySet()) {

			String value = "";
			if (blockState.getValue() instanceof String || blockState.getValue() instanceof Integer) {
				value = blockState.getValue().toString();
			} else if (blockState.getValue() instanceof Byte) {
				byte theByte = (byte) blockState.getValue();
				value = theByte == 0 ? "false" : "true";
			} else {
				System.out.println("Unknown type " + blockState.getValue().getClass());
			}

			bedrockBlockState.properties.put(blockState.getKey(), value);
		}
		return bedrockBlockState;
	}

}
