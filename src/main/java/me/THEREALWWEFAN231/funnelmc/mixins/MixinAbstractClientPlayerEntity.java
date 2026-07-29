package me.THEREALWWEFAN231.funnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayerEntity {

	//TODO: probably want to use PlayerSkinProvider.loadSkin instead, it should work with PlayerListHud as well
	//disabled these because they crash often, kind of
	//also: getSkinTexture()/getModel() were merged into a single getSkin() returning PlayerSkin in modern MC
	@Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
	public void getSkin(CallbackInfoReturnable<PlayerSkin> callbackInfoReturnable) {
		if (Client.instance.isConnectionOpen()) {
			//callbackInfoReturnable.setReturnValue(PlayerListPacketTranslator.skins.get(AbstractClientPlayer.class.cast(this).getGameProfile().getName()).texture);
		}
	}

}
