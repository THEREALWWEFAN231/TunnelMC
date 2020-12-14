package me.THEREALWWEFAN231.tunnelmc.bedrockconnection;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.v422.Bedrock_v422;

import io.netty.util.AsciiString;
import me.THEREALWWEFAN231.tunnelmc.auth.Auth;
import me.THEREALWWEFAN231.tunnelmc.auth.SkinData;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.FakeJavaConnection;
import net.minecraft.client.util.NetworkUtils;

public class Client {

	public static Client instance = new Client();
	private Logger logger = LogManager.getLogger(ClientBatchHandler.class);
	public BedrockPacketCodec bedrockPacketCodec = Bedrock_v422.V422_CODEC;
	private String ip;
	private int port;
	public Auth authData;
	public BedrockClient bedrockClient;
	public FakeJavaConnection javaConnection;

	public void initialize(String ip, int port) {
		this.ip = ip;
		this.port = port;

		InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", this.getOpenLocalPort());
		this.bedrockClient = new BedrockClient(bindAddress);
		this.bedrockClient.bind().join();

		InetSocketAddress addressToConnect = new InetSocketAddress(ip, port);
		this.bedrockClient.connect(addressToConnect).whenComplete(new BiConsumer<BedrockSession, Throwable>() {

			@Override
			public void accept(BedrockSession session, Throwable throwable) {
				if (throwable != null) {
					return;
				}

				Client.this.onSessionInitialized(session);
			}
		}).join();

	}

	public void onSessionInitialized(BedrockSession bedrockSession) {
		bedrockSession.setPacketCodec(this.bedrockPacketCodec);
		bedrockSession.addDisconnectHandler(new Consumer<DisconnectReason>() {

			@Override
			public void accept(DisconnectReason reason) {
				System.out.println("Disconnected");
			}
		});
		bedrockSession.setBatchHandler(new ClientBatchHandler());
		bedrockSession.setLogging(false);

		try {
			LoginPacket loginPacket = new LoginPacket();

			this.authData = new Auth();
			//String chainData = this.authData.getOfflineChainData(TunnelMC.mc.getSession().getUsername());
			String chainData = this.authData.getOnlineChainData();

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
