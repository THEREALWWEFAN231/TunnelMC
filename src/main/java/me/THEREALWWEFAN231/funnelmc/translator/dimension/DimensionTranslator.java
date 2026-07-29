package me.THEREALWWEFAN231.funnelmc.translator.dimension;

import me.THEREALWWEFAN231.funnelmc.javaconnection.VanillaRegistryAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionTranslator {

	public static ResourceKey<Level> bedrockToJavaRegistryKey(int bedrockDimensionId) {
		if (bedrockDimensionId == 0) {
			return Level.OVERWORLD;
		} else if (bedrockDimensionId == 1) {
			return Level.NETHER;
		} else if (bedrockDimensionId == 2) {
			return Level.END;
		}

		return Level.OVERWORLD;
	}

	// Modern DimensionType (26.2) is fully registry/datapack driven, unlike 1.16.5's hardcoded
	// OVERWORLD/THE_NETHER/THE_END static fields - CommonPlayerSpawnInfo needs a real
	// Holder<DimensionType>, not just the dimension's ResourceKey<Level>. There's no real Java
	// server/datapack here to pull one from, so this resolves it from the bundled vanilla default
	// data built by VanillaRegistryAccess instead.
	public static Holder<DimensionType> bedrockToJavaDimensionType(int bedrockDimensionId) {
		Registry<DimensionType> dimensionTypes = VanillaRegistryAccess.get().lookupOrThrow(Registries.DIMENSION_TYPE);

		if (bedrockDimensionId == 1) {
			return dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER);
		} else if (bedrockDimensionId == 2) {
			return dimensionTypes.getOrThrow(BuiltinDimensionTypes.END);
		}

		return dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
	}

}
