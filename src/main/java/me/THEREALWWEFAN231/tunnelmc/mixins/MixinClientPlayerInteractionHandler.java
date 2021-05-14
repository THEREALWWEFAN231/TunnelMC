package me.THEREALWWEFAN231.tunnelmc.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.nukkitx.protocol.bedrock.data.SoundEvent;
import com.nukkitx.protocol.bedrock.packet.LevelSoundEventPacket;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.tunnelmc.utils.PositionUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionHandler {

	@Shadow
	private BlockPos currentBreakingPos;

	@Shadow
	@Final
	private MinecraftClient client;

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
	public void cancelBlockSound(SoundManager soundManager, SoundInstance sound) {
		if (!Client.instance.isConnectionOpen()) {
			// The server should be the one that tells us to make this sound
			soundManager.play(sound);
		} else {
			LevelSoundEventPacket packet = new LevelSoundEventPacket();
			packet.setSound(SoundEvent.HIT);
			packet.setPosition(PositionUtil.toBedrockVector3f(this.currentBreakingPos));
			packet.setExtraData(BlockPaletteTranslator.BLOCK_STATE_TO_RUNTIME_ID.getInt(this.client.world.getBlockState(this.currentBreakingPos)));
			packet.setIdentifier("");
			Client.instance.sendPacket(packet);
		}
	}

}
