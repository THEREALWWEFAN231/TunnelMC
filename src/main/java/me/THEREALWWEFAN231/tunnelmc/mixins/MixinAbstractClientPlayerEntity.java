package me.THEREALWWEFAN231.tunnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.packets.PlayerListPacketTranslator;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {

	//TODO: probably want to use PlayerSkinProvider.loadSkin instead, it should work with PlayerListHud as well
	@Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
	public void getSkinTexture(CallbackInfoReturnable<Identifier> callbackInfoReturnable) {
		if (Client.instance.isConnectionOpen()) {
			callbackInfoReturnable.setReturnValue(PlayerListPacketTranslator.skins.get(AbstractClientPlayerEntity.class.cast(this).getGameProfile().getName()).texture);
		}
	}

	@Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
	public void getModel(CallbackInfoReturnable<String> callbackInfoReturnable) {
		if (Client.instance.isConnectionOpen()) {
			boolean isSlim = PlayerListPacketTranslator.skins.get(AbstractClientPlayerEntity.class.cast(this).getGameProfile().getName()).slim;
			callbackInfoReturnable.setReturnValue(isSlim ? "slim" : "default");
		}
	}

}
