package me.THEREALWWEFAN231.tunnelmc.auth;

import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;

public class Auth {

	public static String getOfflineChainData(String username) {
		Gson gson = TunnelMC.instance.fileManagement.normalGson;
		KeyPair keyPair = EncryptionUtils.createKeyPair();
		PublicKey publicKey = keyPair.getPublic();

		String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		JsonObject jwtHeader = new JsonObject();
		jwtHeader.addProperty("x5u", publicKeyBase64);
		jwtHeader.addProperty("alg", "ES384");

		JsonObject chainDataKeyJsonObject = new JsonObject();
		JsonObject extraData = new JsonObject();
		extraData.addProperty("identity", UUID.randomUUID().toString());
		extraData.addProperty("displayName", username);

		chainDataKeyJsonObject.addProperty("exp", Instant.now().getEpochSecond() + TimeUnit.HOURS.toSeconds(6));
		chainDataKeyJsonObject.addProperty("identityPublicKey", publicKeyBase64);
		chainDataKeyJsonObject.addProperty("nbf", Instant.now().getEpochSecond() + -TimeUnit.HOURS.toSeconds(6));
		chainDataKeyJsonObject.add("extraData", extraData);

		String chainDataKeyString = gson.toJson(chainDataKeyJsonObject);

		String jwt = Auth.base64Encode(gson.toJson(jwtHeader)) + "." + Auth.base64Encode(chainDataKeyString) + "." + Auth.base64Encode(new String(publicKey.getEncoded()));

		JsonObject chainDataJsonObject = new JsonObject();
		JsonArray chainDataJsonArray = new JsonArray();
		chainDataJsonObject.add("chain", chainDataJsonArray);
		chainDataJsonArray.add(jwt);

		return gson.toJson(chainDataJsonObject);
	}

	public static String base64Encode(String input) {
		return new String(Base64.getEncoder().encode(input.getBytes()));
	}

}
