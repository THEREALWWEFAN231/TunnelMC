package me.THEREALWWEFAN231.funnelmc.bedrockconnection;

import java.net.InetSocketAddress;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v1001.Bedrock_v1001;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;
import org.cloudburstmc.protocol.bedrock.data.auth.AuthType;
import org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockClientInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket;
import org.cloudburstmc.protocol.bedrock.packet.NetworkSettingsPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestNetworkSettingsPacket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;

import java.util.List;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.auth.Auth;
import me.THEREALWWEFAN231.funnelmc.auth.SkinData;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.BlockEntityDataCache;
import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainers;
import me.THEREALWWEFAN231.funnelmc.javaconnection.FakeJavaConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;

public class Client {

	public static Client instance = new Client();
	private final Logger logger = LogManager.getLogger(ClientBatchHandler.class);
	public BedrockCodec bedrockCodec = Bedrock_v1001.CODEC;
	private String ip;
	private int port;
	private boolean onlineMode;
	public Auth authData;
	public BedrockClientSession bedrockSession;
	public FakeJavaConnection javaConnection;

	public BedrockContainers containers;
	public BlockEntityDataCache blockEntityDataCache;
	public byte openContainerId;

	// Set from StartGamePacket. Most current servers (including the vanilla Bedrock Dedicated Server
	// by default) run SERVER or SERVER_WITH_REWIND, which reject the legacy client-authoritative
	// MovePlayerPacket outright ("Client cannot send MovePlayerPacket in server-auth movement
	// environment!") - movement has to go through PlayerAuthInputPacket instead. See
	// PlayerAuthInputSender and the movementMode guard in PlayerMoveTranslator.
	public AuthoritativeMovementMode movementMode = AuthoritativeMovementMode.CLIENT;

	private List<String> onlineChainData;

	// The player's Microsoft/Xbox login, obtained once via MicrosoftLoginScreen before the player
	// can even reach the "Connect to Bedrock Server" screen, then reused for every join and for
	// the friends list for the rest of this game session instead of logging in again each time.
	public Auth cachedAuth;
	public List<String> cachedOnlineChainData;
	public String cachedXboxLiveAuthorizationHeader;

	public boolean hasCachedLogin() {
		return this.cachedAuth != null && this.cachedOnlineChainData != null;
	}

	public void setCachedLogin(Auth auth, List<String> onlineChainData, String xboxLiveAuthorizationHeader) {
		this.cachedAuth = auth;
		this.cachedOnlineChainData = onlineChainData;
		this.cachedXboxLiveAuthorizationHeader = xboxLiveAuthorizationHeader;
	}

	// Guards against a connection attempt (or the auth flow leading up to one) being kicked off
	// again while one is already in flight - e.g. a spammed Join Server click/held Enter key
	// previously fired connect() dozens of times in a row, leaking a fresh NioEventLoopGroup each
	// time. Reset once the in-flight attempt finishes, whether it succeeded or failed.
	private volatile boolean connecting = false;

	// Offline mode only - online mode always goes through connectWithExistingAuth, since the
	// player already signed in via MicrosoftLoginScreen before reaching a screen that can call this.
	public void initialize(String ip, int port, boolean onlineMode) {
		if (this.connecting) {
			return;
		}
		this.connecting = true;

		this.ip = ip;
		this.port = port;
		this.onlineMode = onlineMode;
		this.connect();
	}

	// For callers that already logged in (via MicrosoftLoginScreen, cached on Client.instance) -
	// skips the device code flow entirely instead of making the player log in again for every join.
	public void connectWithExistingAuth(String ip, int port, Auth authData, List<String> onlineChainData) {
		if (this.connecting) {
			return;
		}
		this.connecting = true;

		this.ip = ip;
		this.port = port;
		this.onlineMode = true;
		this.authData = authData;
		this.onlineChainData = onlineChainData;
		this.connect();
	}

	private void connect() {
		// Reconnecting without restarting the game (e.g. after a crash, or just trying again) never
		// told the previous session's server we were leaving - the JVM shutdown hook in
		// onSessionInitialized only covers actually quitting the game. Left the old RakNet session
		// dangling server-side until its own timeout, so Geyser/Floodgate's duplicate-login guard
		// rejected every attempt in between with "X is already logged in!".
		if (this.bedrockSession != null && this.bedrockSession.isConnected()) {
			this.bedrockSession.disconnect();
		}

		org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
		logger.get().setLevel(Level.DEBUG);

		InetSocketAddress addressToConnect = new InetSocketAddress(this.ip, this.port);
		NioEventLoopGroup group = new NioEventLoopGroup();

		ChannelFuture future = new Bootstrap()
				.channelFactory(RakChannelFactory.client(NioDatagramChannel.class))
				.option(RakChannelOption.RAK_PROTOCOL_VERSION, this.bedrockCodec.getRaknetProtocolVersion())
				.group(group)
				.handler(new BedrockClientInitializer() {
					@Override
					protected void initSession(BedrockClientSession session) {
						session.setCodec(Client.this.bedrockCodec);
						session.setPacketHandler(new ClientBatchHandler());
						session.setLogging(false);

						Client.this.onSessionInitialized(session);
					}
				})
				.connect(addressToConnect);

		future.addListener(result -> {
			this.connecting = false;
			if (!result.isSuccess()) {
				group.shutdownGracefully();
				Minecraft.getInstance().execute(() -> Minecraft.getInstance().disconnect(new DisconnectedScreen(Minecraft.getInstance().gui.screen(), Component.literal("Use Translated Here"), Component.literal(result.cause().getMessage())), false));
			}
		});
	}

	public void onSessionInitialized(BedrockClientSession bedrockSession) {
		this.bedrockSession = bedrockSession;

		// The codec helper defaults to EncodingSettings.DEFAULT (maxListSize=1536), sized for a
		// generic/server-facing peer - but CreativeContentPacket's item catalog and
		// ItemComponentPacket's per-item component list are both genuinely bigger than that in
		// modern Minecraft (1800+ items/blocks), so real values over 1536 got rejected as if they
		// were corrupt ("Tried to read N bytes but maximum is 1536"), when they were actually just
		// legitimately large. EncodingSettings.CLIENT (maxListSize=10240) is the preset the library
		// itself ships specifically for this - a Bedrock client receiving these large server-sent lists.
		bedrockSession.getPeer().getCodecHelper().setEncodingSettings(EncodingSettings.CLIENT);

		// Without this, quitting or crashing the game leaves the RakNet session dangling from the
		// server's perspective until it times out server-side (tens of seconds) instead of dropping
		// immediately - Geyser/Floodgate's duplicate-login guard then rejects the next connection
		// attempt with "X is already logged in!" until that old session finally expires.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (bedrockSession.isConnected()) {
				bedrockSession.disconnect();
			}
		}));

		// Real Bedrock servers (including Geyser) expect this handshake before LoginPacket - it's
		// how the server learns our protocol version and tells us what compression to use. Skipping
		// straight to LoginPacket leaves the server's pre-negotiation pipeline treating us as an
		// unrecognized/legacy client, which can't parse our (correct, modern) login payload.
		RequestNetworkSettingsPacket requestNetworkSettingsPacket = new RequestNetworkSettingsPacket();
		requestNetworkSettingsPacket.setProtocolVersion(bedrockSession.getCodec().getProtocolVersion());
		this.sendPacketImmediately(requestNetworkSettingsPacket);
	}

	// Called by ClientBatchHandler once the server responds to RequestNetworkSettingsPacket - only
	// safe to send LoginPacket after this, once we're using the compression the server told us to.
	public void onNetworkSettings(NetworkSettingsPacket packet) {
		this.bedrockSession.setCompression(packet.getCompressionAlgorithm());

		try {
			LoginPacket loginPacket = new LoginPacket();

			if (this.onlineMode) {
				loginPacket.setAuthPayload(new CertificateChainPayload(this.onlineChainData, AuthType.FULL));
			} else {
				this.authData = new Auth();
				loginPacket.setAuthPayload(new CertificateChainPayload(this.authData.getOfflineChainData(Minecraft.getInstance().getUser().getName()), AuthType.SELF_SIGNED));
			}

			loginPacket.setProtocolVersion(this.bedrockSession.getCodec().getProtocolVersion());
			loginPacket.setClientJwt(SkinData.getSkinData(this.ip + ":" + this.port));
			this.sendPacketImmediately(loginPacket);

			this.javaConnection = new FakeJavaConnection();

		} catch (Exception e) {
			this.logger.error("Failed to complete Bedrock login / build fake Java connection", e);
		}
	}

	//when our java player is initialized
	public void onPlayerInitialized() {
		this.containers = new BedrockContainers();
		this.blockEntityDataCache = new BlockEntityDataCache();
		this.openContainerId = 0;
	}

	// Bedrock's respawn state machine (CLIENT_SEARCHING -> SERVER_SEARCHING -> SERVER_READY ->
	// CLIENT_READY_TO_SPAWN) also runs as part of the initial join handshake, not just actual player
	// deaths - RespawnPacketTranslator uses this to tell the two apart, since translating the
	// handshake's SERVER_READY into a real Java ClientboundRespawnPacket resets ClientPacketListener's
	// LevelLoadTracker back to its initial WaitingForServer state, undoing LEVEL_CHUNKS_LOAD_START and
	// leaving the "Loading Terrain" screen stuck forever. Starts false and is flipped true by
	// RespawnPacketTranslator itself the first time it sees SERVER_READY (that first occurrence is
	// always the join handshake and gets swallowed instead of translated) - NOT by StartGameTranslator,
	// since the handshake's SERVER_READY packet arrives after StartGamePacket is already done
	// processing, so a flag set there would already read true and never gate anything.
	public boolean initialSpawnComplete = false;

	public boolean isConnectionOpen() {
		return this.bedrockSession != null && this.bedrockSession.isConnected();
	}

	public void sendPacketImmediately(BedrockPacket packet) {
		this.bedrockSession.sendPacketImmediately(packet);
		if (this.bedrockSession.isLogging()) {
			this.logger.info("Outbound {}: {}", this.bedrockSession.getSocketAddress(), packet.getClass().getCanonicalName());
		}
	}

	public void sendPacket(BedrockPacket packet) {
		this.bedrockSession.sendPacket(packet);
		if (this.bedrockSession.isLogging()) {
			this.logger.info("Outbound {}: {}", this.bedrockSession.getSocketAddress(), packet.getClass().getCanonicalName());
		}
	}

}
