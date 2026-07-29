package me.THEREALWWEFAN231.funnelmc.mixins;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;

@Mixin(value = KeyboardHandler.class, priority = 9999)
public class MixinKeyboard {

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void onKeyEvent(long windowPointer, int scanCode, KeyEvent keyEvent, CallbackInfo callbackInfo) {

		if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_F3) && keyEvent.key() != GLFW.GLFW_KEY_UNKNOWN) {
			if (keyEvent.key() == GLFW.GLFW_KEY_K) {
			}
		}
	}
}
