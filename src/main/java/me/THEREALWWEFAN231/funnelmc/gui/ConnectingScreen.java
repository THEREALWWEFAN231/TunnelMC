package me.THEREALWWEFAN231.funnelmc.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

// Shown the instant "Join Server" is clicked, so there's some visible feedback while the raw
// Bedrock connection attempt is in flight - without this the previous screen just sat there
// unchanged with no way to tell whether anything had happened. Client#connect() replaces this with
// a DisconnectedScreen if the attempt fails; nothing currently replaces it on success (that's on
// whatever the translated login packets end up driving once a real connection gets that far).
@Environment(EnvType.CLIENT)
public class ConnectingScreen extends Screen {

	private final Screen parent;
	private final String address;

	public ConnectingScreen(Screen parent, String address) {
		super(Component.literal("Connecting"));
		this.parent = parent;
		this.address = address;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreenAndShow(this.parent))
				.pos(this.width / 2 - 102, this.height / 2 + 20).size(204, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, Component.literal("Connecting to " + this.address + "..."), this.width / 2, this.height / 2 - 10, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

}
