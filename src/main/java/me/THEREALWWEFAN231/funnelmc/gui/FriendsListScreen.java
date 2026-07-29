package me.THEREALWWEFAN231.funnelmc.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import me.THEREALWWEFAN231.funnelmc.auth.XboxLiveApi;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.funnelmc.nethernet.FranchiseAuth;
import me.THEREALWWEFAN231.funnelmc.nethernet.NetherNetDiscovery;
import me.THEREALWWEFAN231.funnelmc.nethernet.NetherNetSignal;
import me.THEREALWWEFAN231.funnelmc.nethernet.NetherNetTransport;
import me.THEREALWWEFAN231.funnelmc.nethernet.PlayFabAuth;
import me.THEREALWWEFAN231.funnelmc.nethernet.SignalingConnection;

// Lists the player's Xbox friends who are currently active in Minecraft and lets them try to join
// one. Reuses the login the player already did via MicrosoftLoginScreen (cached on Client.instance)
// before they could reach this screen, instead of logging in again.
// Sessions with a direct IP/port connect the same way "Connect to Bedrock Server" does. Sessions
// that only offer NetherNet (WebRTC) can't actually be joined yet - see probeNetherNet's comment.
@Environment(EnvType.CLIENT)
public class FriendsListScreen extends Screen {

	private static final Logger logger = LogManager.getLogger(FriendsListScreen.class);

	private final Screen parent;
	private String statusLine = "Looking for friends playing Minecraft...";

	public FriendsListScreen(Screen parent) {
		super(Component.literal("Bedrock Friends"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreenAndShow(this.parent))
				.pos(this.width / 2 - 102, this.height - 40).size(204, 20).build());

		if (!Client.instance.hasCachedLogin()) {
			this.statusLine = "Not logged in - go back and reconnect to sign in.";
			return;
		}

		this.loadFriends();
	}

	private void loadFriends() {
		String xboxLiveAuthorizationHeader = Client.instance.cachedXboxLiveAuthorizationHeader;

		new Thread(() -> {
			try {
				List<XboxLiveApi.Friend> friends = XboxLiveApi.getFriends(xboxLiveAuthorizationHeader);

				List<String> xuids = new ArrayList<>();
				for (XboxLiveApi.Friend friend : friends) {
					xuids.add(friend.xuid);
				}

				List<String> activeXuids = XboxLiveApi.getXuidsActiveInMinecraft(xuids, xboxLiveAuthorizationHeader);

				List<XboxLiveApi.Friend> activeFriends = new ArrayList<>();
				for (XboxLiveApi.Friend friend : friends) {
					if (activeXuids.contains(friend.xuid)) {
						activeFriends.add(friend);
					}
				}

				this.minecraft.execute(() -> this.showFriends(activeFriends));
			} catch (Exception e) {
				logger.error("[FriendsDiag] Failed to load friends", e);
				this.minecraft.execute(() -> this.statusLine = "Failed to load friends: " + e.getMessage());
			}
		}, "FunnelMC-Friends-Load").start();
	}

	private void showFriends(List<XboxLiveApi.Friend> activeFriends) {
		if (activeFriends.isEmpty()) {
			this.statusLine = "None of your friends are currently playing Minecraft.";
			return;
		}

		this.statusLine = "Friends currently playing Minecraft:";

		int y = this.height / 2 - 40;
		for (XboxLiveApi.Friend friend : activeFriends) {
			this.addRenderableWidget(Button.builder(Component.literal(friend.gamertag), button -> this.tryJoin(friend))
					.pos(this.width / 2 - 102, y).size(204, 20).build());
			y += 24;
		}
	}

	private void tryJoin(XboxLiveApi.Friend friend) {
		this.statusLine = "Looking up " + friend.gamertag + "'s world...";
		String xboxLiveAuthorizationHeader = Client.instance.cachedXboxLiveAuthorizationHeader;

		new Thread(() -> {
			try {
				XboxLiveApi.JoinableSession session = XboxLiveApi.findJoinableSession(friend.xuid, xboxLiveAuthorizationHeader);

				if (session == null) {
					this.minecraft.execute(() -> this.statusLine = friend.gamertag + " doesn't have a joinable world right now.");
					return;
				}

				if (session.isDirectIp()) {
					this.minecraft.execute(() -> Client.instance.connectWithExistingAuth(session.hostIp, session.hostPort, Client.instance.cachedAuth, Client.instance.cachedOnlineChainData));
					return;
				}

				if (session.netherNetId == null) {
					this.minecraft.execute(() -> this.statusLine = friend.gamertag + "'s session has no usable connection info.");
					return;
				}

				this.minecraft.execute(() -> this.statusLine = friend.gamertag + "'s world uses NetherNet - probing signaling connection (see funnelmc.log)...");
				this.probeNetherNet(friend, session.netherNetId);
			} catch (Exception e) {
				logger.error("[FriendsDiag] Couldn't join {}", friend.gamertag, e);
				this.minecraft.execute(() -> this.statusLine = "Couldn't join " + friend.gamertag + ": " + e.getMessage());
			}
		}, "FunnelMC-Friends-Join").start();
	}

	// The hostname pattern is "wss://signaling-tm-<region>.franchise.minecraft-services.net" - the
	// "-tm-" strongly suggests Azure Traffic Manager, which picks a region per-connection rather
	// than per-account. A live probe showed our own client landing on "mexicocentral" on one attempt
	// and "eastus2" on another (same machine, minutes apart) - the friend's own client independently
	// got routed to whichever region it saw fit when it started hosting, with no guarantee it matches
	// ours. When it doesn't, that region's signaling server has never heard of the friend's network
	// ID and bounces every message with "Player not found" - not a bug in what we send, just the
	// wrong regional server. Full list from the "qos-beacons" section of the Discovery response.
	private static final List<String> SIGNALING_REGIONS = List.of(
			"eastus2", "mexicocentral", // observed in live probes - tried first
			"australiaEast", "australiaSoutheast", "brazilSouth", "canadaCentral", "centralIndia",
			"centralUs", "eastAsia", "eastUs", "franceCentral", "japanEast", "japanWest",
			"koreaCentral", "northCentralUs", "northEurope", "southAfricaNorth", "southCentralUs",
			"southeastAsia", "swedenCentral", "uaeNorth", "ukSouth", "westCentralUs", "westEurope",
			"westUs", "westUs2", "westUs3");

	// This world can only be reached over NetherNet (WebRTC). The auth chain (Xbox Live -> PlayFab
	// -> franchise MCToken -> real STUN/TURN Credentials) was already proven working end to end in
	// an earlier build - that part doesn't depend on region, so it's only done once here. What does
	// depend on region is the signaling websocket itself (see SIGNALING_REGIONS above), so this
	// tries each region in turn: open a persistent signaling connection there, wait for Credentials,
	// have NetherNetTransport create the data channels, send our SDP offer, and give it a few
	// seconds to either reach a real answer or bounce with "Player not found" before moving on.
	private void probeNetherNet(XboxLiveApi.Friend friend, long netherNetId) {
		new Thread(() -> {
			try {
				String gameVersion = Client.instance.bedrockCodec.getMinecraftVersion();
				logger.warn("[NetherNetDiag] Starting probe for {}'s session (netherNetId={}, gameVersion={})", friend.gamertag, netherNetId, gameVersion);

				NetherNetDiscovery.DiscoveryResult discovery = NetherNetDiscovery.discover(gameVersion);
				String playFabXboxToken = Client.instance.cachedAuth.getXboxTokenForRelyingParty("rp://playfabapi.com/");
				PlayFabAuth.LoginResult playFabLogin = PlayFabAuth.login(discovery.auth.playFabTitleId, playFabXboxToken);
				FranchiseAuth.Token mcToken = FranchiseAuth.startSession(discovery.auth.serviceUri, discovery.auth.playFabTitleId, playFabLogin.sessionTicket, gameVersion);
				logger.warn("[NetherNetDiag] Auth chain ready, trying {} signaling regions", SIGNALING_REGIONS.size());

				for (String region : SIGNALING_REGIONS) {
					String signalingUri = "wss://signaling-tm-" + region + ".franchise.minecraft-services.net";
					this.minecraft.execute(() -> this.statusLine = "Trying " + region + " for " + friend.gamertag + "'s world (see funnelmc.log)...");

					if (this.attemptNetherNetJoin(signalingUri, mcToken.authorizationHeader, netherNetId, friend)) {
						return;
					}
				}

				this.minecraft.execute(() -> this.statusLine = "Couldn't reach " + friend.gamertag + "'s world on any known NetherNet signaling region.");
			} catch (Exception e) {
				logger.error("[NetherNetDiag] Probe failed", e);
				this.minecraft.execute(() -> this.statusLine = "NetherNet probe failed: " + e.getMessage());
			}
		}, "FunnelMC-NetherNet-Probe").start();
	}

	// Returns true once the data channel reaches OPEN via this specific signaling region. Actually
	// routing Bedrock packets over it (replacing the RakNet/UDP pipeline for this connection) is
	// still the next piece of work - reaching OPEN here only proves the WebRTC transport itself.
	private boolean attemptNetherNetJoin(String signalingUri, String authorizationHeader, long netherNetId, XboxLiveApi.Friend friend) {
		logger.warn("[NetherNetDiag] Trying signaling region {}", signalingUri);

		String targetNetworkId = Long.toUnsignedString(netherNetId);
		AtomicReference<NetherNetTransport> transportRef = new AtomicReference<>();
		SignalingConnection signaling;

		try {
			signaling = SignalingConnection.connect(signalingUri, authorizationHeader, new SignalingConnection.Listener() {
				@Override
				public void onCredentials(String credentialsJson) {
				}

				@Override
				public void onSignal(NetherNetSignal signal, String fromNetworkId) {
					NetherNetTransport t = transportRef.get();
					if (t != null && targetNetworkId.equals(fromNetworkId)) {
						t.handleSignal(signal);
					}
				}

				@Override
				public void onClosed(int statusCode, String reason) {
				}
			});
		} catch (Exception e) {
			logger.warn("[NetherNetDiag] Region {} - couldn't open signaling connection: {}", signalingUri, e.getMessage());
			return false;
		}

		NetherNetTransport transport = null;
		try {
			String credentialsJson = signaling.awaitCredentials(15);

			transport = new NetherNetTransport(signaling, netherNetId, credentialsJson);
			transportRef.set(transport);
			transport.connect();

			transport.whenDataChannelOpen().get(8, TimeUnit.SECONDS);
			logger.warn("[NetherNetDiag] Data channel OPEN via {} - WebRTC transport is fully connected!", signalingUri);
			this.minecraft.execute(() -> this.statusLine = "NetherNet data channel connected via " + signalingUri + "! (packet pipeline not wired up yet - see funnelmc.log)");
			return true;
		} catch (Exception e) {
			logger.warn("[NetherNetDiag] Region {} did not connect: {}", signalingUri, e.getMessage());
			return false;
		} finally {
			if (transport != null) {
				transport.close();
			}
			signaling.close();
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFFFF);
		graphics.centeredText(this.font, Component.literal(this.statusLine), this.width / 2, this.height / 2 - 60, 0xFFCCCCCC);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}

}
