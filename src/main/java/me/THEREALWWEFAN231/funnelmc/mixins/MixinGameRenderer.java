package me.THEREALWWEFAN231.funnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.utils.ScaffoldPlace;
import net.minecraft.client.Minecraft;

// The crosshair/targeted-entity raycast used to be computed in GameRenderer#updateTargetedEntity,
// but that method no longer exists in modern MC - the hitResult/crosshairPickEntity fields moved
// onto Minecraft itself, computed by the private Minecraft#pick(float) method.
@Mixin(Minecraft.class)
public class MixinGameRenderer {

	@Inject(method = "pick", at = @At("RETURN"))
	public void pick(float tickDelta, CallbackInfo callbackInfo) {
		if (FunnelMC.mc.getCameraEntity() == null || !Client.instance.isConnectionOpen()) {
			return;
		}
		ScaffoldPlace.setRaycastResult();
	}

}
