package me.THEREALWWEFAN231.tunnelmc.bedrockconnection;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.v448.Bedrock_v448;

import io.netty.util.AsciiString;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.auth.Auth;
import me.THEREALWWEFAN231.tunnelmc.auth.SkinData;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.BlockEntityDataCache;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainers;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.FakeJavaConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.text.Text;

public class Client {

	public static Client instance = new Client();
	private final Logger logger = LogManager.getLogger(ClientBatchHandler.class);
	public BedrockPacketCodec bedrockPacketCodec = Bedrock_v448.V448_CODEC;
	private String ip;
	private int port;
	private boolean onlineMode;
	public Auth authData;
	public BedrockClient bedrockClient;
	public FakeJavaConnection javaConnection;
	
	public BedrockContainers containers;
	public BlockEntityDataCache blockEntityDataCache;
	public byte openContainerId;

	public void initialize(String ip, int port, boolean onlineMode) {
		this.ip = ip;
		this.port = port;
		this.onlineMode = onlineMode;

		org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
		logger.get().setLevel(Level.DEBUG);

		InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", this.getOpenLocalPort());
		this.bedrockClient = new BedrockClient(bindAddress);
		this.bedrockClient.bind().join();

		InetSocketAddress addressToConnect = new InetSocketAddress(ip, port);
		this.bedrockClient.connect(addressToConnect).whenComplete((BiConsumer<BedrockSession, Throwable>) (session, throwable) -> {
			if (throwable != null) {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(new DisconnectedScreen(MinecraftClient.getInstance().currentScreen, Text.of("Use Translated Here"), Text.of(throwable.getMessage()))));
				return;
			}

			Client.this.onSessionInitialized(session);
		});

	}

	public void onSessionInitialized(BedrockSession bedrockSession) {
		bedrockSession.setPacketCodec(this.bedrockPacketCodec);
		bedrockSession.addDisconnectHandler(reason -> System.out.println("Disconnected"));
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

			loginPacket.setProtocolVersion(bedrockSession.getPacketCodec().getProtocolVersion());
			loginPacket.setChainData(new AsciiString(chainData.getBytes()));
			loginPacket.setSkinData(new AsciiString(SkinData.getSkinData(this.ip + ":" + this.port)));
			this.sendPacketImmediately(loginPacket);

			this.javaConnection = new FakeJavaConnection();

		} catch (Exception e) {
			//TODO: do something better with this
			e.printStackTrace();
		}
	}
	
	//when our java player is initialized
	public void onPlayerInitialized() {
		this.containers = new BedrockContainers();
		this.blockEntityDataCache = new BlockEntityDataCache();
		this.openContainerId = 0;
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

		session.sendPacket(packet);
		if (session.isLogging()) {
			this.logger.info("Outbound {}: {}", session.getAddress().toString(), packet.getClass().getCanonicalName());
		}
	}

	public int getOpenLocalPort() {
		int port = NetworkUtils.findLocalPort();//minecraft does this when opening world to lan
		if (port == 25564) {//fallback port, we are going to change it because we are cool
			port = 2021;
		}
		return port;
		//		return 12345;
	}

}
