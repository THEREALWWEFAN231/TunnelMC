package me.THEREALWWEFAN231.tunnelmc.translator.packets;

import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

import javax.crypto.SecretKey;

import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.protocol.bedrock.packet.ClientToServerHandshakePacket;
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;

import me.THEREALWWEFAN231.tunnelmc.auth.Auth;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;

public class ServerToClientHandshakePacketTranslator extends PacketTranslator<ServerToClientHandshakePacket> {

	@Override
	public void translate(ServerToClientHandshakePacket packet) {
		//thanks to proxypass
		/*try {
			SignedJWT saltJwt = SignedJWT.parse(packet.getJwt());
			URI x5u = saltJwt.getHeader().getX509CertURL();
			ECPublicKey serverKey = EncryptionUtils.generateKey(x5u.toASCIIString());
			SecretKey key = EncryptionUtils.getSecretKey(Auth.lastKeypair.getPrivate(), serverKey, Base64.getDecoder().decode(saltJwt.getJWTClaimsSet().getStringClaim("salt")));
			Client.instance.bedrockClient.getSession().enableEncryption(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ClientToServerHandshakePacket clientToServerHandshake = new ClientToServerHandshakePacket();
        Client.instance.sendPacketImmediately(clientToServerHandshake);*/

	}

	@Override
	public Class<?> getPacketClass() {
		return ServerToClientHandshakePacket.class;
	}

}
