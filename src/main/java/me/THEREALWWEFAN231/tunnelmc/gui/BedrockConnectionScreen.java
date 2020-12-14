package me.THEREALWWEFAN231.tunnelmc.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
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
	
	private ButtonWidget selectServerButton;
	private TextFieldWidget addressField;
	private TextFieldWidget portField;
	private BooleanConsumer callback;
	private final Screen parent;

	public BedrockConnectionScreen(Screen parent, BooleanConsumer callback) {
		super(new LiteralText("Bedrock Connection"));
		this.parent = parent;
		this.callback = callback;
	}

	public void tick() {
		this.addressField.tick();
		this.portField.tick();
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if ((this.getFocused() != this.addressField && this.getFocused() != this.portField)
				|| keyCode != 257 && keyCode != 335) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		} else {
			return true;
		}
	}

	protected void init() {
		this.client.keyboard.setRepeatEvents(true);
		this.selectServerButton = (ButtonWidget) this.addButton(new ButtonWidget(this.width / 2 - 102,
				this.height / 4 + 100 + 12, 204, 20, new TranslatableText("selectServer.select"), (buttonWidget) -> {
					int port;
					try {
						port = Integer.parseInt(portField.getText());
					} catch (NumberFormatException e) {
						port = 19132;
					}
					Client.instance.initialize(addressField.getText(), port);
				}));
		this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 120 + 12, 204, 20, ScreenTexts.CANCEL,
				(buttonWidget) -> {
					this.callback.accept(false);
				}));
		this.addressField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 116, 200, 20,
				new LiteralText("Enter IP"));
		this.portField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 140, 200, 20,
				new LiteralText("Enter Port"));
		this.addressField.setMaxLength(128);
		this.portField.setMaxLength(6);
		this.addressField.setSelected(true);
		this.portField.setSelected(false);
		this.addressField.setText("");
		this.portField.setText("19132");
		this.addressField.setChangedListener((text) -> {
			this.onAddressFieldChanged();
		});
		this.children.add(this.addressField);
		this.children.add(this.portField);
		this.setInitialFocus(this.addressField);
		this.onAddressFieldChanged();
	}

	public void resize(MinecraftClient client, int width, int height) {
		String string = this.addressField.getText();
		String string1 = this.portField.getText();
		this.init(client, width, height);
		this.addressField.setText(string);
		this.portField.setText(string1);
	}

	public void onClose() {
		this.client.openScreen(this.parent);
	}

	public void removed() {
		this.client.keyboard.setRepeatEvents(false);
		this.client.options.write();
	}

	private void onAddressFieldChanged() {
		String string = this.addressField.getText();
		this.selectServerButton.active = !string.isEmpty() && string.split(":").length > 0 && string.indexOf(32) == -1;
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
		drawTextWithShadow(matrices, this.textRenderer, new LiteralText("Enter IP and Port"), this.width / 2 - 100, 100,
				10526880);
		this.addressField.render(matrices, mouseX, mouseY, delta);
		this.portField.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
	}
}