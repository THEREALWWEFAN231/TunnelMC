package me.THEREALWWEFAN231.tunnelmc.auth;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.Signature;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;

public class Auth {

	//currently doesn't work, based off https://github.com/Sandertv/gophertunnel
	public static String getOnlineChainData() {
		try {
			Gson gson = TunnelMC.instance.fileManagement.normalGson;

			PrivateKey privateKey = new PrivateKey();

			Xbox xbox = new Xbox();
			String userToken = xbox.userToken(privateKey.publicKey());
			String deviceToken = xbox.deviceToken(privateKey.publicKey(), privateKey);
			String titleToken = xbox.titleToken(privateKey.publicKey(), privateKey, deviceToken);
			JsonObject xsts = xbox.xstsToken(userToken, deviceToken, titleToken);

			String chainData = xbox.RequestMinecraftChain(xsts, privateKey.publicKey());
			JsonObject chainDataFromMinecraftNet = TunnelMC.instance.fileManagement.jsonParser.parse(chainData).getAsJsonObject();
			JsonArray oldChain = chainDataFromMinecraftNet.get("chain").getAsJsonArray();
			String firstChainHeader = oldChain.get(0).getAsString();
			firstChainHeader = firstChainHeader.split("\\.")[0];
			firstChainHeader = new String(Base64.getDecoder().decode(firstChainHeader.getBytes()));
			String firstKeyx5u = TunnelMC.instance.fileManagement.jsonParser.parse(firstChainHeader).getAsJsonObject().get("x5u").getAsString();

			JsonObject newFirstChain = new JsonObject();
			newFirstChain.addProperty("certificateAuthority", true);
			newFirstChain.addProperty("exp", Instant.now().getEpochSecond() + TimeUnit.HOURS.toSeconds(6));
			newFirstChain.addProperty("identityPublicKey", firstKeyx5u);
			newFirstChain.addProperty("nbf", Instant.now().getEpochSecond() + -TimeUnit.HOURS.toSeconds(6));
			String newFirstChainString = gson.toJson(newFirstChain);

			{
				JsonObject jwtHeader = new JsonObject();
				jwtHeader.addProperty("alg", "ES384");
				System.out.println(privateKey.publicKey().toDer().getBytes().length);
				//this isn't right, i dont think so, something to do with asn1 /:
				jwtHeader.addProperty("x5u", Base64.getEncoder().withoutPadding().encodeToString(privateKey.publicKey().toDer().getBytes()));

				String header = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(jwtHeader).getBytes());
				String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(newFirstChainString.getBytes());
				String signatureString = null;

				byte[] dataToSign = (header + "." + payload).getBytes();
				MessageDigest digest = MessageDigest.getInstance("SHA-384");
				byte[] hashBytes = digest.digest(dataToSign);

				Signature signature = Ecdsa.signTunnelMc(hashBytes, privateKey);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				byteArrayOutputStream.write(Xbox.bigIntegerToByteArray(signature.r));
				byteArrayOutputStream.write(Xbox.bigIntegerToByteArray(signature.s));
				signatureString = Base64.getUrlEncoder().withoutPadding().encodeToString(byteArrayOutputStream.toByteArray());

				String jwt = header + "." + payload + "." + signatureString;

				JsonArray newChainData = new JsonArray();
				newChainData.add(jwt);
				for (JsonElement jsonElement : oldChain) {
					newChainData.add(jsonElement.getAsString());
				}
				chainDataFromMinecraftNet.add("chain", newChainData);
			}

			chainData = gson.toJson(chainDataFromMinecraftNet);

			System.out.println(chainData);

			return chainData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BedrockSessionData getSessionDataFromChainData(String chainData) {

		JsonObject chainDataObject = TunnelMC.instance.fileManagement.jsonParser.parse(chainData).getAsJsonObject();
		JsonArray chain = chainDataObject.getAsJsonArray("chain").getAsJsonArray();
		String importantChainDataBase64 = chain.get(chain.size() - 1).getAsString();
		importantChainDataBase64 = importantChainDataBase64.split("\\.")[1];//get the middle jwt thing
		String importantChainData = new String(Base64.getDecoder().decode(importantChainDataBase64.getBytes()));

		JsonObject importantCahinDataObject = TunnelMC.instance.fileManagement.jsonParser.parse(importantChainData).getAsJsonObject();
		JsonObject extraData = importantCahinDataObject.get("extraData").getAsJsonObject();
		String xuid = extraData.has("XUID") ? extraData.get("XUID").getAsString() : "";//for offline sessions
		UUID identity = UUID.fromString(extraData.get("identity").getAsString());
		String displayName = extraData.get("displayName").getAsString();

		return new BedrockSessionData(xuid, identity, displayName);
	}

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
	
	private static final KeyPairGenerator KEY_PAIR_GEN;

	static {
		try {
			KEY_PAIR_GEN = KeyPairGenerator.getInstance("EC");
			KEY_PAIR_GEN.initialize(new ECGenParameterSpec("secp256r1"));//use P-256
		} catch (Exception e) {
			throw new AssertionError("Unable to initialize required encryption", e);
		}
	}

	public static KeyPair createKeyPair() {
		return KEY_PAIR_GEN.generateKeyPair();
	}

}
