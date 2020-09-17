package me.THEREALWWEFAN231.tunnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkmagician6.eventapi.EventManager;

import me.THEREALWWEFAN231.tunnelmc.events.EventPlayerTick;
import net.minecraft.client.network.ClientPlayerEntity;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"))
	public void tick(CallbackInfo callbackInfo) {
		EventManager.call(new EventPlayerTick());
	}
	
}
