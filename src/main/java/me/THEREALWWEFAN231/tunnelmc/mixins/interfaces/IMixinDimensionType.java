package me.THEREALWWEFAN231.tunnelmc.mixins.interfaces;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.dimension.DimensionType;

@Mixin(DimensionType.class)
public interface IMixinDimensionType {

	@Accessor("OVERWORLD")
	public static DimensionType getOverworld() {
		return null;
	}

	@Accessor("THE_NETHER")
	public static DimensionType getTheNether() {
		return null;
	}

	@Accessor("THE_END")
	public static DimensionType getTheEnd() {
		return null;
	}

}
