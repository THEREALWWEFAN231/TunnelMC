package me.THEREALWWEFAN231.funnelmc.translator.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.enchantment.EnchantmentTranslator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.Identifier;

public class ItemTranslator {

	//key, is the item id:damage, so for example could be 218:13
	public static final HashMap<String, Item> BEDROCK_ITEM_INFO_TO_JAVA_ITEM = new HashMap<>();

	public static void load() {

		JsonObject jsonObject = FunnelMC.instance.fileManagement.getJsonObjectFromResource("geyser/items.json");
		if (jsonObject == null) {
			throw new RuntimeException("Items list not found!");
		}

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String javaStringIdentifier = entry.getKey();
			Identifier javaIdentifier = Identifier.parse(javaStringIdentifier);

			JsonObject bedrockItemData = entry.getValue().getAsJsonObject();
			int bedrockId = bedrockItemData.get("bedrock_id").getAsInt();
			int bedrockData = bedrockItemData.get("bedrock_data").getAsInt();

			Item item = BuiltInRegistries.ITEM.getValue(javaIdentifier);

			if (item == Items.AIR && !javaStringIdentifier.equals("minecraft:air")) {//item not found
				System.out.println(javaStringIdentifier + " item was not found, this generally isn't good.");
				continue;
			}

			BEDROCK_ITEM_INFO_TO_JAVA_ITEM.put(bedrockId + ":" + bedrockData, item);
		}

	}

	//TODO: tags and what ever
	public static ItemStack itemDataToItemStack(ItemData itemData) {

		// An empty slot/no-item ItemData has no ItemDefinition at all (nothing to look up), unlike
		// items that just failed to resolve to a known definition - treat it the same as Items.AIR
		// instead of NPEing on getDefinition().getIdentifier().
		if (itemData.getDefinition() == null) {
			return ItemStack.EMPTY;
		}

		// The legacy BEDROCK_ITEM_INFO_TO_JAVA_ITEM table is keyed by a fixed numeric bedrock_id, but
		// modern Bedrock items are identified via ItemDefinition with a runtime id negotiated per
		// session - that numeric id no longer lines up with anything in geyser/items.json, so it was
		// always a near-guaranteed lookup miss (returning null, then crashing the ItemStack
		// constructor). Most item identifiers match directly between editions, so look up by the
		// definition's identifier instead - BuiltInRegistries.ITEM falls back to Items.AIR for an
		// unmatched identifier rather than returning null (see the same pattern in ItemTranslator#load).
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemData.getDefinition().getIdentifier()));
		ItemStack itemStack = new ItemStack(item);
		itemStack.setCount(itemData.getCount());

		if (itemData.getTag() != null) {
			List<NbtMap> bedrockEnchantments = itemData.getTag().getList("ench", NbtType.COMPOUND, null);
			if (bedrockEnchantments != null) {

				// TODO: this needs real registry data - see DimensionTranslator/StartGameTranslator
				// TODOs. Enchantments are a datapack-driven registry now, so without a real
				// RegistryAccess this lookup will always come back empty.
				Optional<Registry<Enchantment>> enchantmentRegistry = FunnelMC.mc.player.level().registryAccess().lookup(Registries.ENCHANTMENT);

				for (NbtMap enchantmentData : bedrockEnchantments) {
					int bedrockEnchantmentId = enchantmentData.getShort("id");
					int enchantmentLevel = enchantmentData.getShort("lvl");

					ResourceKey<Enchantment> javaEnchantmentKey = EnchantmentTranslator.BEDROCK_TO_JAVA_ENCHANTMENTS.get(bedrockEnchantmentId);
					if (javaEnchantmentKey == null) {
						System.out.println("Enchantment " + bedrockEnchantmentId + " not found");
						continue;
					}

					Holder<Enchantment> javaEnchantment = enchantmentRegistry
							.flatMap(registry -> registry.get(javaEnchantmentKey.identifier()))
							.map(reference -> (Holder<Enchantment>) reference)
							.orElse(null);
					if (javaEnchantment == null) {
						continue;
					}

					itemStack.enchant(javaEnchantment, enchantmentLevel);
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

		NbtMap nbtMap = NbtMap.builder().putInt("Damage", 1).build();

		// TODO: modern Bedrock items are identified via ItemDefinition (identifier + runtime id
		// negotiated per session, see itemDataToItemStack above) and blocks via BlockDefinition -
		// this SimpleItemDefinition is a placeholder using the legacy numeric id as both.
		ItemDefinition itemDefinition = new SimpleItemDefinition(idDamageString, Integer.parseInt(idDamageSplit[0]), false);
		ItemData itemData = ItemData.builder().definition(itemDefinition).damage(Integer.parseInt(idDamageSplit[1])).count(itemStack.getCount()).tag(nbtMap).build();

		return itemData;
	}

}
