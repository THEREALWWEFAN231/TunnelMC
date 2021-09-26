package me.THEREALWWEFAN231.tunnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nukkitx.protocol.bedrock.packet.InteractPacket;
import com.nukkitx.protocol.bedrock.packet.InteractPacket.Action;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(CallbackInfo callback) {
		TunnelMC.instance.initialize();
	}

	@Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;addBlockBreakingParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)V"))
	public void onBlockBreaking(ParticleManager particleManager, BlockPos pos, Direction direction) {
		if (!Client.instance.isConnectionOpen()) {
			// Don't let the client add this - let the server
			particleManager.addBlockBreakingParticles(pos, direction);
		}
	}

	//inventory opened, I could have swore there was some packet for this(that could be translated) I can't find it, I am so confused, found it!!! ClientCommandC2SPacket ClientCommandC2SPacket.Mode.OPEN_INVENTORY, packet might only be sent when the player is riding an entity/
	@Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 1))
	private void handleInputEvents(CallbackInfo callbackInfo) {
		if (Client.instance.isConnectionOpen()) {
			
			InteractPacket interactPacket = new InteractPacket();
			interactPacket.setAction(Action.OPEN_INVENTORY);
			interactPacket.setRuntimeEntityId(TunnelMC.mc.player.getId());

			Client.instance.sendPacket(interactPacket);
		}
	}

}
