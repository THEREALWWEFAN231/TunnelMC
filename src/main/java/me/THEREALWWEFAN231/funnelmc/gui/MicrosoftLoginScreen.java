package me.THEREALWWEFAN231.funnelmc.gui;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import me.THEREALWWEFAN231.funnelmc.auth.Auth;
import me.THEREALWWEFAN231.funnelmc.auth.AuthTokenStore;
import me.THEREALWWEFAN231.funnelmc.auth.DeviceCodeAuth;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;

// Mandatory Microsoft/Xbox sign-in gate shown before the player can reach the "Connect to Bedrock
// Server" screen at all. Logs in once via the device code flow, caches the result on Client.instance
// (so BedrockConnectionScreen and FriendsListScreen can both reuse it for the rest of the session),
// then hands off to whatever screen the caller wants to show next.
@Environment(EnvType.CLIENT)
public class MicrosoftLoginScreen extends Screen {

	private final Screen parent;
	private final Supplier<Screen> nextScreen;
	private Button copyCodeButton;

	private String statusLine = "Requesting a login code from Microsoft...";
	private String codeLine = "";
	private DeviceCodeAuth.DeviceCodeInfo deviceCodeInfo;

	public MicrosoftLoginScreen(Screen parent, Supplier<Screen> nextScreen) {
		super(Component.literal("Log in with Microsoft"));
		this.parent = parent;
		this.nextScreen = nextScreen;
	}

	@Override
	protected void init() {
		this.copyCodeButton = this.addRenderableWidget(Button.builder(Component.literal("Copy Code"), button -> {
			if (this.deviceCodeInfo != null) {
				this.minecraft.keyboardHandler.setClipboard(this.deviceCodeInfo.userCode);
			}
		}).pos(this.width / 2 - 102, this.height / 2 + 20).size(204, 20).build());
		this.copyCodeButton.active = false;

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreenAndShow(this.parent))
				.pos(this.width / 2 - 102, this.height / 2 + 45).size(204, 20).build());

		this.startLogin();
	}

	private void startLogin() {
		new Thread(() -> {
			try {
				// Skip the device code screen entirely if we've already got a refresh token saved
				// from a previous launch - silent, no player interaction needed. Only fall back to
				// the full device code flow if there isn't one saved yet, or it's been revoked/expired.
				String savedRefreshToken = AuthTokenStore.loadRefreshToken();
				if (savedRefreshToken != null) {
					this.minecraft.execute(() -> this.statusLine = "Signing in with your saved login...");

					try {
						this.finishLogin(DeviceCodeAuth.refreshAccessToken(savedRefreshToken));
						return;
					} catch (Exception e) {
						// Saved token no longer works - fall through to the normal device code flow.
					}
				}

				this.deviceCodeInfo = DeviceCodeAuth.requestDeviceCode();
				this.minecraft.execute(() -> {
					this.statusLine = "Go to " + this.deviceCodeInfo.verificationUri + " and enter the code below:";
					this.codeLine = this.deviceCodeInfo.userCode;
					this.copyCodeButton.active = true;
				});

				this.finishLogin(DeviceCodeAuth.pollForAccessToken(this.deviceCodeInfo));
			} catch (Exception e) {
				e.printStackTrace();
				this.minecraft.execute(() -> {
					this.statusLine = "Login failed: " + e.getMessage();
					this.codeLine = "";
					this.copyCodeButton.active = false;
				});
			}
		}, "FunnelMC-Login").start();
	}

	private void finishLogin(DeviceCodeAuth.TokenResponse tokens) throws Exception {
		AuthTokenStore.saveRefreshToken(tokens.refreshToken);

		Auth auth = new Auth();
		java.util.List<String> onlineChainData = auth.getOnlineChainData(tokens.accessToken);
		String xboxLiveAuthorizationHeader = auth.getXboxLiveAuthorizationHeader();
		Client.instance.setCachedLogin(auth, onlineChainData, xboxLiveAuthorizationHeader);

		this.minecraft.execute(() -> {
			this.statusLine = "Logged in!";
			this.codeLine = "";
			this.copyCodeButton.active = false;
			this.minecraft.setScreenAndShow(this.nextScreen.get());
		});
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
		graphics.centeredText(this.font, Component.literal(this.statusLine), this.width / 2, this.height / 2 - 30, 0xFFCCCCCC);
		if (!this.codeLine.isEmpty()) {
			graphics.centeredText(this.font, Component.literal(this.codeLine), this.width / 2, this.height / 2 - 10, 0xFFFFFF55);
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

}
