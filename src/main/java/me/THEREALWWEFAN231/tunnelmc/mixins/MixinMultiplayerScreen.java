package me.THEREALWWEFAN231.tunnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.gui.BedrockConnectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen {

	protected MixinMultiplayerScreen(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At(value = "RETURN"))
	public void init(CallbackInfo callback) {
		this.addButton(new ButtonWidget(5, 5, 150, 20, new LiteralText("Connect To Bedrock Server"), (buttonWidget) -> {
			TunnelMC.mc.openScreen(new BedrockConnectionScreen(this));
		}));
	}

}
