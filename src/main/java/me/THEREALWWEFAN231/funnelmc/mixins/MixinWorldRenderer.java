package me.THEREALWWEFAN231.funnelmc.mixins;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Block-breaking progress tracking moved from WorldRenderer to ClientLevel#destroyBlockProgress
// in modern MC (no more render()-loop hooking needed - see LevelEventTranslator, which now
// calls ClientLevel#destroyBlockProgress directly to drive Bedrock-side breaking progress).
@Mixin(ClientLevel.class)
public class MixinWorldRenderer {

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"), cancellable = true)
    public void cancelBlockBreakingInfo(int entityId, BlockPos pos, int stage, CallbackInfo ci) {
        if (Client.instance.isConnectionOpen()) {
            // Don't let the client set this - let the server (via LevelEventTranslator)
            ci.cancel();
        }
    }
}
