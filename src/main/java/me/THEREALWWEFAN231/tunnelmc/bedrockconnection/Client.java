package me.THEREALWWEFAN231.tunnelmc.bedrockconnection;

import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.data.GameType;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.v431.Bedrock_v431;
import io.netty.util.AsciiString;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.auth.Auth;
import me.THEREALWWEFAN231.tunnelmc.auth.ClientData;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.BlockEntityDataCache;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainers;
import me.THEREALWWEFAN231.tunnelmc.gui.BedrockConnectingScreen;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.FakeJavaConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.function.BiConsumer;

public class Client {

	public static Client instance = new Client();

	public BedrockPacketCodec bedrockPacketCodec = Bedrock_v431.V431_CODEC;
	private final Logger logger = LogManager.getLogger(ClientBatchHandler.class);

	private String ip;
	private int port;

	public GameType defaultGameMode;
	private boolean onlineMode;

	public Auth authData;

	public BedrockClient bedrockClient;
	public BedrockConnectingScreen connectScreen;

	public FakeJavaConnection javaConnection;
	
	public BedrockContainers containers;
	public BlockEntityDataCache blockEntityDataCache;

	public int entityRuntimeId;
	public byte openContainerId = 0;

	public void initialize(String ip, int port, boolean onlineMode) {
		this.ip = ip;
		this.port = port;
		this.onlineMode = onlineMode;

		org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
		logger.get().setLevel(Level.DEBUG);

		InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", getRandomPort());
		this.bedrockClient = new BedrockClient(bindAddress);
		this.bedrockClient.bind().join();
		this.connectScreen = new BedrockConnectingScreen(MinecraftClient.getInstance().currentScreen, MinecraftClient.getInstance(), this.bedrockClient);
		TunnelMC.mc.openScreen(this.connectScreen);

		InetSocketAddress addressToConnect = new InetSocketAddress(ip, port);
		this.bedrockClient.connect(addressToConnect).whenComplete((BiConsumer<BedrockSession, Throwable>) (session, throwable) -> {
			if (throwable != null) {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
						new DisconnectedScreen(
								new TitleScreen(false),
								Text.of("TunnelMC"),
								Text.of(throwable.getMessage())
						)
				));
				return;
			}

			this.connectScreen.setStatus(Text.of("Logging in..."));
			Client.this.onSessionInitialized(session);
		});

	}

	private int getRandomPort() {
		try (DatagramSocket datagramSocket = new DatagramSocket(0)) {
			return datagramSocket.getLocalPort();
		} catch(SocketException e) {
			throw new RuntimeException("Could not open socket to find next free port", e);
		}
	}

	public void onSessionInitialized(BedrockSession bedrockSession) {
		bedrockSession.setPacketCodec(this.bedrockPacketCodec);
		bedrockSession.addDisconnectHandler(reason -> MinecraftClient.getInstance().execute(() -> {
			// We disconnected ourselves.
			if (reason == DisconnectReason.DISCONNECTED) {
				return;
			}

			MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
					new DisconnectedScreen(
							new TitleScreen(false),
							Text.of("TunnelMC"),
							Text.of("You were disconnected from the target server because: " + reason.toString())
					)
			));
		}));

		bedrockSession.setBatchHandler(new ClientBatchHandler());
		bedrockSession.setLogging(false);

		try {
			LoginPacket loginPacket = new LoginPacket();

			this.authData = new Auth();
			String chainData;
			if (this.onlineMode) {
				chainData = this.authData.getOnlineChainData();
			} else {
				chainData = this.authData.getOfflineChainData(TunnelMC.mc.getSession().getUsername());
			}

			String clientData = ClientData.getClientData(this.ip + ":" + this.port);
			if (clientData == null) {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
						new DisconnectedScreen(
								new TitleScreen(false),
								Text.of("TunnelMC"),
								Text.of("There was an error when generating client data. Please try again later.")
						)
				));
				return;
			}

			loginPacket.setProtocolVersion(bedrockSession.getPacketCodec().getProtocolVersion());
			loginPacket.setChainData(new AsciiString(chainData.getBytes()));
			loginPacket.setSkinData(new AsciiString(clientData));
			this.sendPacketImmediately(loginPacket);

			this.connectScreen.setStatus(Text.of("Loading resources..."));

			this.javaConnection = new FakeJavaConnection();
		} catch (Exception e) {
			MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
					new DisconnectedScreen(
							new TitleScreen(false),
							Text.of("TunnelMC"),
							Text.of(e.getMessage())
					)
			));
		}
	}

	public void onPlayerInitialized() {
		this.containers = new BedrockContainers();
		this.blockEntityDataCache = new BlockEntityDataCache();
	}

	public boolean isConnectionOpen() {
		return this.bedrockClient != null && this.bedrockClient.getRakNet() != null && this.bedrockClient.getRakNet().isRunning();
	}

	public void sendPacketImmediately(BedrockPacket packet) {
		BedrockSession session = this.bedrockClient.getSession();

		session.sendPacketImmediately(packet);
		if (session.isLogging()) {
			this.logger.info("Outbound {}: {}", session.getAddress().toString(), packet.getClass().getCanonicalName());
		}
	}

	public void sendPacket(BedrockPacket packet) {
		BedrockSession session = this.bedrockClient.getSession();

		if (session != null) {
			session.sendPacket(packet);
			if (session.isLogging()) {
				this.logger.info("Outbound {}: {}", session.getAddress().toString(), packet.getClass().getCanonicalName());
			}
		}
	}

}