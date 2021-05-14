package me.THEREALWWEFAN231.tunnelmc.mixins;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {//this was is is for testing purposes, renders the slot ids over slots, so we can visualize them, add to the mixin json if you want to see it

	@Inject(method = "drawSlot", at = @At("RETURN"))
	private void drawSlot(MatrixStack matrices, Slot slot, CallbackInfo callbackInfo) {
		matrices.push();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);//pop above items, this is never good, but as this is for testing purposes it's fine, altough it might actually be safe to use 2d, I know there are problems in 3d
		//matrices.translate(0, 0, 1000);//pop above items, for what ever reason distorts the color
		TunnelMC.mc.textRenderer.drawWithShadow(matrices, slot.id + "", slot.x + 4, slot.y + 4, 0xffff0000);
		matrices.pop();
	}

}
