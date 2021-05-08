package me.THEREALWWEFAN231.tunnelmc.gui;

import net.minecraft.client.gui.widget.CheckboxWidget;
import org.lwjgl.glfw.GLFW;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class BedrockConnectionScreen extends Screen {

	//	The UI that comes up after you click on Connect To Bedrock.

	private ButtonWidget joinServerButton;
	private TextFieldWidget addressField;
	private TextFieldWidget portField;
	private CheckboxWidget onlineModeWidget;
	private final Screen parent;

	public BedrockConnectionScreen(Screen parent) {
		super(new LiteralText("Bedrock Connection"));
		this.parent = parent;
	}

	public void init() {
		this.client.keyboard.setRepeatEvents(true);
		this.joinServerButton = this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 100 + 12, 204, 20, new TranslatableText("selectServer.select"), button -> {
			if (BedrockConnectionScreen.this.addressField.getText().isEmpty()) {
				return;
			}

			int port;
			try {
				port = Integer.parseInt(BedrockConnectionScreen.this.portField.getText());
			} catch (NumberFormatException e) {
				port = 19132;
			}

			Client.instance.initialize(BedrockConnectionScreen.this.addressField.getText(), port, BedrockConnectionScreen.this.onlineModeWidget.isChecked());
		}));

		this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 120 + 12, 204, 20, ScreenTexts.CANCEL,
				button -> BedrockConnectionScreen.this.client.openScreen(BedrockConnectionScreen.this.parent)));
		this.addressField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 116, 200, 20, new LiteralText("Enter IP"));
		this.portField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 140, 200, 20, new LiteralText("Enter Port"));
		this.onlineModeWidget = new CheckboxWidget(this.width / 2 - 100, 164, 200, 20, new LiteralText("Online mode"), true);
		this.addressField.setMaxLength(128);
		this.portField.setMaxLength(6);
		this.addressField.setSelected(true);
		this.portField.setSelected(false);
		this.addressField.setText("127.0.0.1");
		this.portField.setText("19132");
		this.addressField.setChangedListener(text -> BedrockConnectionScreen.this.onAddressFieldChanged());
		this.children.add(this.addressField);
		this.children.add(this.portField);
		this.setInitialFocus(this.addressField);
		this.onAddressFieldChanged();
	}

	public void tick() {
		this.addressField.tick();
		this.portField.tick();
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		Screen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
		Screen.drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Enter IP and Port"), this.width / 2 - 100, 100, 10526880);
		this.addressField.render(matrices, mouseX, mouseY, delta);
		this.portField.render(matrices, mouseX, mouseY, delta);
		this.onlineModeWidget.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {

		this.addressField.mouseClicked(mouseX, mouseY, button);
		this.portField.mouseClicked(mouseX, mouseY, button);
		this.onlineModeWidget.mouseClicked(mouseX, mouseY, button);

		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if ((this.addressField.isFocused() || this.portField.isFocused()) && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
			this.joinServerButton.onPress();
			this.joinServerButton.playDownSound(this.client.getSoundManager());
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public void resize(MinecraftClient client, int width, int height) {
		String addressText = this.addressField.getText();
		String portText = this.portField.getText();
		this.init(client, width, height);
		this.addressField.setText(addressText);
		this.portField.setText(portText);
	}

	public void onClose() {
		this.client.openScreen(this.parent);
	}

	public void removed() {
		this.client.keyboard.setRepeatEvents(false);
		this.client.options.write();
	}

	private void onAddressFieldChanged() {
		String addressText = this.addressField.getText();
		this.joinServerButton.active = !addressText.isEmpty() && addressText.split(":").length > 0 && addressText.indexOf(32) == -1;
	}

}