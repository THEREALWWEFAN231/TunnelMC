package me.THEREALWWEFAN231.tunnelmc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nukkitx.nbt.NbtList;
import com.nukkitx.nbt.NbtMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockTranslator {

	//the name, then the runtime id
	public static final HashMap<BlockState, Integer> BLOCKSTATE_RUNTIMEID_MAP = new HashMap<BlockState, Integer>();
	public static final HashMap<Integer, BlockState> RUNTIMEID_BLOCKSTATE_MAP = new HashMap<Integer, BlockState>();

	public static void createDevelopmentJson(NbtList<NbtMap> blockPaletteData) {//creates a json that needs to be fixed!!!
		JsonArray mainJavaBlockStatesJsonArray = new JsonArray();

		for (NbtMap nbtMap : blockPaletteData) {

			String mcbeStringBlockName = nbtMap.getCompound("block").getString("name");

			JsonObject jsonEntry = new JsonObject();
			mainJavaBlockStatesJsonArray.add(jsonEntry);

			JsonObject bedrockData = new JsonObject();
			jsonEntry.add("Bedrock Data", bedrockData);

			bedrockData.addProperty("Name", mcbeStringBlockName);
			NbtMap blockStates = nbtMap.getCompound("block").getCompound("states");

			JsonObject states = new JsonObject();
			for (Map.Entry<String, Object> blockState : blockStates.entrySet()) {

				String stateKey = blockState.getKey();
				Object stateValue = blockState.getValue();
				if (stateValue instanceof Number) {
					states.addProperty(stateKey, (Number) stateValue);
				} else if (stateValue instanceof String) {
					states.addProperty(stateKey, (String) stateValue);
				} else {
					System.out.println("Unknown type value for block state " + stateValue.getClass());
				}
			}

			if (states.size() != 0) {
				bedrockData.add("States", states);
			}

			JsonObject javaData = new JsonObject();
			jsonEntry.add("Java Data", javaData);

			Identifier minecraftBlockIdentifier = new Identifier(mcbeStringBlockName.toLowerCase());//for now we use toLowerCase, as minecraft is strict, remove this when we get stuff fully working
			Block foundBlock = Registry.BLOCK.get(minecraftBlockIdentifier);

			if (foundBlock == Blocks.AIR && nbtMap.getShort("id") != 0) {//so Registry.BLOCK.get returns air if it did not find a block, so we check if the block we are looking for isn't air, so if it returns air and the block isn't air, we have a "problem"
				javaData.addProperty("Name", "not_found_fix_this");
			} else {
				javaData.addProperty("Name", minecraftBlockIdentifier.toString());
			}
		}

		try {
			Files.write(TunnelMC.instance.fileManagement.formattedGson.toJson(mainJavaBlockStatesJsonArray).getBytes(), new File("C://users/THEREALWWEFAN231/downloads/TunnelMC/src/main/resources/java block states.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadMap() {

		String textInFile = null;
		try {
			textInFile = TunnelMC.instance.fileManagement.getTextFromFile(new File("C://users/THEREALWWEFAN231/desktop/TunnelMC/src/main/resources/java block states.json"));
		} catch (Exception e) {
			System.out.println("Failed to read java block states.json");
			return;
		}

		JsonArray jsonArray = TunnelMC.instance.fileManagement.jsonParser.parse(textInFile).getAsJsonArray();

		int blockRuntimeId = 0;
		for (JsonElement jsonElement : jsonArray) {
			JsonObject entry = jsonElement.getAsJsonObject();

			JsonObject bedrockData = entry.get("Bedrock Data").getAsJsonObject();
			JsonObject javaData = entry.get("Java Data").getAsJsonObject();

			String javaBlockIdentifier = javaData.get("Name").getAsString();

			BlockState blockState = null;
			if (javaBlockIdentifier.equals("not_found_fix_this")) {
				blockState = Blocks.STONE.getDefaultState();
			} else {
				blockState = Registry.BLOCK.get(new Identifier(javaBlockIdentifier)).getDefaultState();
			}

			BlockTranslator.BLOCKSTATE_RUNTIMEID_MAP.put(blockState, blockRuntimeId);
			BlockTranslator.RUNTIMEID_BLOCKSTATE_MAP.put(blockRuntimeId, blockState);

			blockRuntimeId += 1;
		}

	}

}
