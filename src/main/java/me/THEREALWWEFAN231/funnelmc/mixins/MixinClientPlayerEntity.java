package me.THEREALWWEFAN231.funnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkmagician6.eventapi.EventManager;

import me.THEREALWWEFAN231.funnelmc.events.EventPlayerTick;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public class MixinClientPlayerEntity {

	// Reverted: fully freezing the local player's position every tick (regardless of server
	// authority) also zeroed out the position delta PlayerAuthInputSender reports each tick, since
	// that delta is computed from the player's actual position - so WASD/jump input stopped
	// producing any movement at all, worse than the rubber-banding this was meant to fix. Real
	// Bedrock clients keep running local physics prediction even under server-authoritative
	// movement and report the predicted delta; the server reconciles via MovePlayerPacket
	// corrections (already handled in MovePlayerPacketTranslator), it doesn't expect a client that
	// never moves on its own.
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V"))
	public void tick(CallbackInfo callbackInfo) {
		EventManager.call(new EventPlayerTick());
	}

}
