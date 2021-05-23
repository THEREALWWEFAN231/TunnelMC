package me.THEREALWWEFAN231.tunnelmc.translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public class EntityTranslator {

	public static final HashMap<String, EntityType<?>> BEDROCK_IDENTIFIER_TO_ENTITY_TYPE = new HashMap<>();

	public static void load() {
		List<EntityType<?>> allEntityTypes = Registry.ENTITY_TYPE.stream().collect(Collectors.toList());

		for (EntityType<?> e : allEntityTypes) {
			BEDROCK_IDENTIFIER_TO_ENTITY_TYPE.put(EntityType.getId(e).toString(), e);
		}

		JsonObject jsonObject = TunnelMC.instance.fileManagement.getJsonObjectFromResource("tunnel/entity_override_translations.json");
		if (jsonObject == null) {
			return;
		}

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			Optional<EntityType<?>> optional = EntityType.get(entry.getValue().getAsString());
			if (!optional.isPresent()) {
				System.out.println("Could not find entity type " + entry.getValue().getAsString() + " when reading entity_override_translations.json");
				continue;
			}

			BEDROCK_IDENTIFIER_TO_ENTITY_TYPE.put(entry.getKey(), optional.get());
		}

	}

}