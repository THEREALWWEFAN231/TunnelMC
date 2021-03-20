package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

import javax.crypto.SecretKey;

import com.google.gson.JsonObject;
import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.protocol.bedrock.packet.ClientToServerHandshakePacket;
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

public class ServerToClientHandshakePacketTranslator extends PacketTranslator<ServerToClientHandshakePacket> {

	@Override
	public void translate(ServerToClientHandshakePacket packet) {
		// Thanks to proxypass, manually parse the jwt, as said in Xbox, this would be easier using the jwt library but I like seeing what's actually happening
		try {
			String[] jwtSplit = packet.getJwt().split("\\.");
			String header = new String(Base64.getDecoder().decode(jwtSplit[0]));
			JsonObject headerObject = TunnelMC.instance.fileManagement.jsonParser.parse(header).getAsJsonObject();
			
			String payload = new String(Base64.getDecoder().decode(jwtSplit[1]));
			JsonObject payloadObject = TunnelMC.instance.fileManagement.jsonParser.parse(payload).getAsJsonObject();
			
			ECPublicKey serverKey = EncryptionUtils.generateKey(headerObject.get("x5u").getAsString());
			SecretKey key = EncryptionUtils.getSecretKey(Client.instance.authData.getPrivateKey(), serverKey, Base64.getDecoder().decode(payloadObject.get("salt").getAsString()));
			Client.instance.bedrockClient.getSession().enableEncryption(key);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ClientToServerHandshakePacket clientToServerHandshake = new ClientToServerHandshakePacket();
		Client.instance.sendPacketImmediately(clientToServerHandshake);

	}

	@Override
	public Class<?> getPacketClass() {
		return ServerToClientHandshakePacket.class;
	}

}
