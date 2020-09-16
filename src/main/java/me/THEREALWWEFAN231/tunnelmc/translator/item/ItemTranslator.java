package me.THEREALWWEFAN231.tunnelmc.translator.item;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemTranslator {

	//key, is the item id:damage, so for example could be 218:13
	public static final HashMap<String, Item> BEDROCK_ITEM_INFO_TO_JAVA_ITEM = new HashMap<String, Item>();

	public static void load() {

		JsonObject jsonObject = TunnelMC.instance.fileManagement.getJsonObjectFromResource("geyser/items.json");
		if (jsonObject == null) {
			return;
		}

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String javaStringIdentifier = entry.getKey();
			Identifier javaIdentifier = new Identifier(javaStringIdentifier);

			JsonObject bedrockItemData = entry.getValue().getAsJsonObject();
			int bedrockId = bedrockItemData.get("bedrock_id").getAsInt();
			int bedrockData = bedrockItemData.get("bedrock_data").getAsInt();

			Item item = Registry.ITEM.get(javaIdentifier);

			if (item == Items.AIR && !javaStringIdentifier.equals("minecraft:air")) {//item not found
				System.out.println(javaStringIdentifier + " item was not found, this generally isn't good.");
				continue;
			}

			BEDROCK_ITEM_INFO_TO_JAVA_ITEM.put(bedrockId + ":" + bedrockData, item);
		}

	}

	//TODO: tags and what ever
	public static ItemStack itemDataToItemStack(ItemData itemData) {

		//keep the short cast, the server can send us non short numbers that, "need to be rolled over" to their correct id
		ItemStack itemStack = new ItemStack(BEDROCK_ITEM_INFO_TO_JAVA_ITEM.get((short) itemData.getId() + ":" + itemData.getDamage()));
		itemStack.setCount(itemData.getCount());

		return itemStack;
	}

}
