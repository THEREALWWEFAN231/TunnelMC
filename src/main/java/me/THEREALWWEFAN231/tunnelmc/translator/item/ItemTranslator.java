package me.THEREALWWEFAN231.tunnelmc.translator.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.enchantment.EnchantmentTranslator;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemTranslator {

	//key, is the item id:damage, so for example could be 218:13
	public static final HashMap<String, Item> BEDROCK_ITEM_INFO_TO_JAVA_ITEM = new HashMap<>();

	public static void load() {
		JsonObject jsonObject = TunnelMC.instance.fileManagement.getJsonObjectFromResource("geyser/items.json");
		if (jsonObject == null) {
			throw new RuntimeException("Items list not found!");
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
		int damage = 0;
		if (itemData.getTag() != null) {
			damage = itemData.getTag().getInt("Damage");
		}

		//keep the short cast, the server can send us non short numbers that, "need to be rolled over" to their correct id
		ItemStack itemStack = new ItemStack(BEDROCK_ITEM_INFO_TO_JAVA_ITEM.get((short) itemData.getId() + ":" + damage));
		itemStack.setCount(itemData.getCount());

		if (itemData.getTag() != null) {
			List<NbtMap> bedrockEnchantments = itemData.getTag().getList("ench", NbtType.COMPOUND, null);
			if (bedrockEnchantments != null) {
				
				for(NbtMap enchantmentData : bedrockEnchantments) {
					int bedrockEnchantmentId = enchantmentData.getShort("id");
					int enchantmentLevel = enchantmentData.getShort("lvl");
					
					Enchantment javaEnchantment = EnchantmentTranslator.BEDROCK_TO_JAVA_ENCHANTMENTS.get(bedrockEnchantmentId);
					if(javaEnchantment == null) {
						System.out.println("Enchantment " + bedrockEnchantmentId + " not found");
						continue;
					}

					itemStack.addEnchantment(javaEnchantment, enchantmentLevel);
				}
				
			}
		}

		return itemStack;
	}

	//TODO: tags and what ever
	public static ItemData itemStackToItemData(ItemStack itemStack) {
		String idDamageString = null;
		for (Map.Entry<String, Item> entry : BEDROCK_ITEM_INFO_TO_JAVA_ITEM.entrySet()) {

			if (entry.getValue().equals(itemStack.getItem())) {
				idDamageString = entry.getKey();
				break;
			}

		}

		if (idDamageString == null) {
			System.out.println("ouch");
		}

		String[] idDamageSplit = idDamageString.split(":");

		int blockRuntimeId = BlockPaletteTranslator.BLOCK_STATE_TO_RUNTIME_ID.getInt(Block.getBlockFromItem(itemStack.getItem()).getDefaultState());

		NbtMap nbtMap = NbtMap.builder().putInt("Damage", 1).build();

		ItemData itemData = ItemData.builder().id(Integer.parseInt(idDamageSplit[0])).damage(Integer.parseInt(idDamageSplit[1])).count(itemStack.getCount()).tag(nbtMap).blockRuntimeId(blockRuntimeId).build();

		return itemData;
	}

}
