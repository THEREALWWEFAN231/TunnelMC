package me.THEREALWWEFAN231.tunnelmc.translator.blockstate;

import java.util.HashMap;
import java.util.Map;

import com.nukkitx.nbt.NbtList;
import com.nukkitx.nbt.NbtMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/*
 * as of 1.16.100, the block palette is static between all servers, so we can load this once and be over it
 * it uses BlockStateTranslator which loaded blocks.json, from BlockStateTranslator we can match blocks and get their runtime id for a Bedrock server
 */
public class BlockPaletteTranslator {

	public static int AIR_BEDROCK_BLOCK_ID;

	public static final HashMap<Integer, BlockState> RUNTIME_ID_TO_BLOCK_STATE = new HashMap<Integer, BlockState>();

	public static void loadMap(NbtList<NbtMap> blockPaletteData) {
		int runtimeId = 0;
		for (NbtMap nbtMap : blockPaletteData) {
			String mcbeStringBlockName = nbtMap.getCompound("block").getString("name");
			NbtMap blockStates = nbtMap.getCompound("block").getCompound("states");

			BedrockBlockState bedrockBlockState = new BedrockBlockState();
			bedrockBlockState.identifier = mcbeStringBlockName;

			for (Map.Entry<String, Object> blockState : blockStates.entrySet()) {

				String value = "";
				if (blockState.getValue() instanceof String || blockState.getValue() instanceof Integer) {
					value = blockState.getValue().toString();
				} else if (blockState.getValue() instanceof Byte) {//i guess byte on mcbe nbt is a boolean type
					byte theByte = (byte) blockState.getValue();
					value = theByte == 0 ? "false" : "true";//im assuming 0 is false
				} else {
					System.out.println("Unknown type " + blockState.getValue().getClass());
				}

				bedrockBlockState.properties.put(blockState.getKey(), value);
			}

			BlockState blockState = BlockStateTranslator.BEDROCK_BLOCK_STATE_STRING_TO_JAVA_BLOCK_STATE.get(bedrockBlockState.toString());
			if (blockState != null) {
				RUNTIME_ID_TO_BLOCK_STATE.put(runtimeId, blockState);
				if (mcbeStringBlockName.equals("minecraft:air")) {
					AIR_BEDROCK_BLOCK_ID = runtimeId;
				}
			} else {
				RUNTIME_ID_TO_BLOCK_STATE.put(runtimeId, Blocks.STONE.getDefaultState());//we could probably put the default state, but for now we will use stone
				//System.out.println("Unknown block " + mcbeStringBlockName + " sent from the servers' palette states=" + bedrockBlockState.toString());
			}

			runtimeId++;
		}

	}

}
