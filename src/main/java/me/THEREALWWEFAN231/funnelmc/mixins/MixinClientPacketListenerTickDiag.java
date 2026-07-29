package me.THEREALWWEFAN231.funnelmc.mixins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;

// Temporary instrumentation for the "stuck on WaitingForPlayerChunk despite playerSectionReady=true"
// investigation - confirms whether ClientPacketListener.tick() (which drives
// LevelLoadTracker.tickClientLoad()) is actually being invoked at all for our fake connection.
// Remove once that investigation is resolved.
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListenerTickDiag {

	private static final Logger logger = LogManager.getLogger(MixinClientPacketListenerTickDiag.class);
	private static int headCount;
	private static int tailCount;

	@Inject(method = "tick", at = @At("HEAD"))
	private void funnelmc$onTickHead(CallbackInfo ci) {
		headCount++;
		if (headCount % 20 == 0) {
			logger.warn("[CPLTickDiag] tick() HEAD reached, headCount={} tailCount={}", headCount, tailCount);
		}
	}

	// If headCount keeps climbing but tailCount never does (or falls behind), tick() is throwing
	// partway through - before ever reaching the levelLoadTracker.tickClientLoad() call at the end.
	@Inject(method = "tick", at = @At("RETURN"))
	private void funnelmc$onTickReturn(CallbackInfo ci) {
		tailCount++;
	}

}
