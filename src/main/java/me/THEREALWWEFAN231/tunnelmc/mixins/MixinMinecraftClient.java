package me.THEREALWWEFAN231.tunnelmc.mixins;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(CallbackInfo callback) {
		TunnelMC.instance.initialize();
	}

	@Redirect(method = "handleBlockBreaking",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/particle/ParticleManager;addBlockBreakingParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)V"))
	public void onBlockBreaking(ParticleManager particleManager, BlockPos pos, Direction direction) {
		if (!Client.instance.isConnectionOpen()) {
			// Don't let the client add this - let the server
			particleManager.addBlockBreakingParticles(pos, direction);
		}
	}

}
