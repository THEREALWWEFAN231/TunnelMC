package me.THEREALWWEFAN231.tunnelmc.javaconnection;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class FakeJavaConnection {

	private ClientConnection clientConnection;//TODO: i think we dont need this to be in the "public scope"
	private ClientPlayNetworkHandler clientPlayNetworkHandler;
	public JavaPacketTranslatorManager packetTranslatorManager;

	public FakeJavaConnection() {
		this.clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
		this.clientPlayNetworkHandler = new ClientPlayNetworkHandler(TunnelMC.mc, null, this.clientConnection, new GameProfile(Client.instance.currentSessionData.getIdentity(), Client.instance.currentSessionData.getDisplayName()));
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
