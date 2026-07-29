package me.THEREALWWEFAN231.funnelmc.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.funnelmc.utils.PositionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;

@Mixin(MultiPlayerGameMode.class)
public class MixinClientPlayerInteractionHandler {

	@Shadow
	private BlockPos destroyBlockPos;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"))
	public SoundEngine.PlayResult cancelBlockSound(SoundManager soundManager, SoundInstance sound) {
		if (!Client.instance.isConnectionOpen()) {
			// The server should be the one that tells us to make this sound
			return soundManager.play(sound);
		} else {
			LevelSoundEventPacket packet = new LevelSoundEventPacket();
			packet.setSound(SoundEvent.HIT);
			packet.setPosition(PositionUtil.toBedrockVector3f(this.destroyBlockPos));
			packet.setExtraData(BlockPaletteTranslator.BLOCK_STATE_TO_RUNTIME_ID.getInt(this.minecraft.level.getBlockState(this.destroyBlockPos)));
			packet.setIdentifier("");
			Client.instance.sendPacket(packet);
			return null;
		}
	}

}
