package me.THEREALWWEFAN231.tunnelmc.translator.packet;

import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.packet.ClientToServerHandshakePacket;
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

import javax.crypto.SecretKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

public class ServerToClientHandshakePacketTranslator extends PacketTranslator<ServerToClientHandshakePacket> {

	@Override
	public void translate(ServerToClientHandshakePacket packet) {
		// Thanks to ProxyPass for this portion of the code.
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