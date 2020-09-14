package me.THEREALWWEFAN231.tunnelmc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
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
		this.addButton(new ButtonWidget(5, 5, 50, 20, new LiteralText("Server"), new ButtonWidget.PressAction() {

			@Override
			public void onPress(ButtonWidget button) {
				//Server.instance.initialize();
			}
		}));

		this.addButton(new ButtonWidget(60, 5, 50, 20, new LiteralText("Client"), new ButtonWidget.PressAction() {

			@Override
			public void onPress(ButtonWidget button) {
				Client.instance.initialize("127.0.0.1", 19132);
			}
		}));
	}

}
