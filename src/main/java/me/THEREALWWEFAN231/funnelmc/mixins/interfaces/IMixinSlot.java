package me.THEREALWWEFAN231.funnelmc.mixins.interfaces;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//this can probably be removed when/if MixinScreenHandler doesn't use @Overwrite
@Mixin(net.minecraft.world.inventory.Slot.class)
public interface IMixinSlot {

	@Invoker("onSwapCraft")
	public void invokeOnSwapCraft(int amount);

}
