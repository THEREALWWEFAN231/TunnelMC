package me.THEREALWWEFAN231.tunnelmc.javaconnection;

import com.mojang.authlib.GameProfile;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.*;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class FakeJavaConnection {

	private final ClientConnection clientConnection;//TODO: i think we dont need this to be in the "public scope"
	private final ClientPlayNetworkHandler clientPlayNetworkHandler;
	public JavaPacketTranslatorManager packetTranslatorManager;

	public FakeJavaConnection() {
		this.clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
		this.clientPlayNetworkHandler = new ClientPlayNetworkHandler(TunnelMC.mc, null, this.clientConnection, new GameProfile(Client.instance.authData.getIdentity(), Client.instance.authData.getDisplayName()));
		this.packetTranslatorManager = new JavaPacketTranslatorManager();
	}

	public void processServerToClientPacket(Packet<ClientPlayPacketListener> packet) {
		//this is what minecraft does, ClientConnection.channelRead0()V
		try {
			packet.apply(this.clientPlayNetworkHandler);
		} catch (OffThreadException e) {
		}
	}

}
