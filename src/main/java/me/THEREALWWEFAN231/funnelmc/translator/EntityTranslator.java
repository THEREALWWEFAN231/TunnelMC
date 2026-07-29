package me.THEREALWWEFAN231.funnelmc.translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class EntityTranslator {

	public static final HashMap<String, EntityType<?>> BEDROCK_IDENTIFIER_TO_ENTITY_TYPE = new HashMap<String, EntityType<?>>();

	public static void load() {

		List<EntityType<?>> allEntityTypes = BuiltInRegistries.ENTITY_TYPE.stream().collect(Collectors.toList());

		for (EntityType<?> e : allEntityTypes) {
			BEDROCK_IDENTIFIER_TO_ENTITY_TYPE.put(EntityType.getKey(e).toString(), e);
		}

		JsonObject jsonObject = FunnelMC.instance.fileManagement.getJsonObjectFromResource("funnelmc/entity override translations.json");
		if (jsonObject == null) {
			return;
		}

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {

			Optional<Holder.Reference<EntityType<?>>> optional = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(entry.getValue().getAsString()));
			if (!optional.isPresent()) {
				System.out.println("Could not find entity type " + entry.getValue().getAsString() + " when reading entity override translations.json");
				continue;
			}

			BEDROCK_IDENTIFIER_TO_ENTITY_TYPE.put(entry.getKey(), optional.get().value());
		}

	}

}
