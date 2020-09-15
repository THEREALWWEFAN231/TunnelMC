package me.THEREALWWEFAN231.tunnelmc.translator.blockstate;

import java.util.HashMap;
import java.util.Map;

import com.nukkitx.nbt.NbtList;
import com.nukkitx.nbt.NbtMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/*
 * so this translates a servers block palette to java editions, each bedrock server can have a different block palette(not sure why, very irrelevant?)
 * it uses BlockStateTranslator which loaded blocks.json, from BlockStateTranslator we can match blocks and get their runtime id for a server
 */
public class ServerBlockPaletteTranslator {

	public static final HashMap<Integer, BlockState> RUNTIME_ID_TO_BLOCK_STATE = new HashMap<Integer, BlockState>();

	public static void loadMap(NbtList<NbtMap> blockPaletteData) {
		RUNTIME_ID_TO_BLOCK_STATE.clear();

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
			} else {
				RUNTIME_ID_TO_BLOCK_STATE.put(runtimeId, Blocks.STONE.getDefaultState());//we could probably put the default state, but for now we will use stone
				//System.out.println("Unknown block " + mcbeStringBlockName + " sent from the servers' palette states=" + bedrockBlockState.toString());
			}

			runtimeId++;
		}

	}

}
