package me.THEREALWWEFAN231.funnelmc.gui;

import org.lwjgl.glfw.GLFW;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BedrockConnectionScreen extends Screen {

	//	The UI that comes up after you click on Connect To Bedrock.

	private Button joinServerButton;
	private EditBox addressField;
	private EditBox portField;
	private Checkbox onlineModeWidget;
	private final Screen parent;

	public BedrockConnectionScreen(Screen parent) {
		super(Component.literal("Bedrock Connection"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.joinServerButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), button -> {
			this.tryJoin();
		}).pos(this.width / 2 - 102, this.height / 4 + 100 + 12).size(204, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreenAndShow(this.parent))
				.pos(this.width / 2 - 102, this.height / 4 + 125 + 12).size(204, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Bedrock Friends"), button -> this.minecraft.setScreenAndShow(new FriendsListScreen(this)))
				.pos(this.width / 2 - 102, this.height / 4 + 150 + 12).size(204, 20).build());

		this.addressField = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 100, (this.height / 4) + 16, 200, 20, Component.literal("Enter IP")));
		this.portField = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 100, (this.height / 4) + 46, 200, 20, Component.literal("Enter Port")));
		this.onlineModeWidget = this.addRenderableWidget(Checkbox.builder(Component.literal("Online mode"), this.font)
				.pos(this.width / 2 - 100, (this.height / 4) + 80).selected(true).build());

		this.addressField.setMaxLength(128);
		this.portField.setMaxLength(6);
		this.addressField.setFocused(true);
		this.portField.setFocused(false);
		this.addressField.setValue("127.0.0.1");
		this.portField.setValue("19132");
		this.addressField.setResponder(text -> this.onAddressFieldChanged());
		this.setInitialFocus(this.addressField);
		this.onAddressFieldChanged();
	}

	private void tryJoin() {
		if (this.addressField.getValue().isEmpty()) {
			return;
		}

		int port;
		try {
			port = Integer.parseInt(this.portField.getValue());
		} catch (NumberFormatException e) {
			port = 19132;
		}

		String address = this.addressField.getValue();
		int finalPort = port;
		boolean onlineMode = this.onlineModeWidget.selected();

		ConnectingScreen connectingScreen = new ConnectingScreen(this, address + ":" + finalPort);
		this.minecraft.setScreenAndShow(connectingScreen);

		if (onlineMode) {
			// The player already signed in via MicrosoftLoginScreen before they could reach this
			// screen, so joining doesn't need to log in again. If the cached login is somehow
			// missing (shouldn't happen given the gate), fall back to logging in first.
			if (Client.instance.hasCachedLogin()) {
				Client.instance.connectWithExistingAuth(address, finalPort, Client.instance.cachedAuth, Client.instance.cachedOnlineChainData);
			} else {
				this.minecraft.setScreenAndShow(new MicrosoftLoginScreen(this, () -> {
					Client.instance.connectWithExistingAuth(address, finalPort, Client.instance.cachedAuth, Client.instance.cachedOnlineChainData);
					return connectingScreen;
				}));
			}
		} else {
			Client.instance.initialize(address, finalPort, false);
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if ((this.addressField.isFocused() || this.portField.isFocused()) && (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER)) {
			this.tryJoin();
			this.joinServerButton.playDownSound(this.minecraft.getSoundManager());
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

	@Override
	public void removed() {
		this.minecraft.options.save();
	}

	private void onAddressFieldChanged() {
		String addressText = this.addressField.getValue();
		this.joinServerButton.active = !addressText.isEmpty() && addressText.split(":").length > 0 && addressText.indexOf(32) == -1;
	}

}
