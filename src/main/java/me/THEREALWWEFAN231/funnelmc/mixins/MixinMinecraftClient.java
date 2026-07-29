package me.THEREALWWEFAN231.funnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cloudburstmc.protocol.bedrock.packet.InteractPacket;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(CallbackInfo callback) {
		FunnelMC.instance.initialize();
	}

	//inventory opened, I could have swore there was some packet for this(that could be translated) I can't find it, I am so confused, found it!!! ClientCommandC2SPacket ClientCommandC2SPacket.Mode.OPEN_INVENTORY, packet might only be sent when the player is riding an entity/
	// The inventory key handling in handleKeybinds() now opens the screen via this.gui.setScreen(...)
	// (Gui#setScreen) instead of Minecraft#setScreenAndShow - that's the only setScreen call in the
	// method, so no ordinal is needed to disambiguate it.
	@Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
	private void handleInputEvents(CallbackInfo callbackInfo) {
		if (Client.instance.isConnectionOpen()) {

			InteractPacket interactPacket = new InteractPacket();
			interactPacket.setAction(InteractPacket.Action.OPEN_INVENTORY);
			interactPacket.setRuntimeEntityId(FunnelMC.mc.player.getId());

			Client.instance.sendPacket(interactPacket);
		}
	}

}
