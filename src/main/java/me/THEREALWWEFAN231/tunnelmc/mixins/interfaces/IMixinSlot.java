package me.THEREALWWEFAN231.tunnelmc.mixins.interfaces;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//this can probably be removed when/if MixinScreenHandler doesn't use @Overwrite
@Mixin(net.minecraft.screen.slot.Slot.class)
public interface IMixinSlot {
	
	@Invoker("onTake")
	public void invokeOnTake(int amount);

}
