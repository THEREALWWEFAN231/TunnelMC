package me.THEREALWWEFAN231.funnelmc.auth;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;

//based off https://github.com/Sandertv/gophertunnel/tree/master/minecraft/auth
public class Auth {

	private ECPublicKey publicKey;
	private ECPrivateKey privateKey;
	private String xuid;
	private UUID identity;
	private String displayName;

	// Retained from getOnlineChainData() so getXboxLiveAuthorizationHeader() can request further
	// XSTS tokens (e.g. for the general Xbox Live API used by the friends list) without repeating
	// the user/device/title token exchange.
	private Xbox xbox;
	private String userToken;
	private String deviceToken;
	private String titleToken;
	private ECPublicKey xboxLiveKeyPublicKey;
	private ECPrivateKey xboxLiveKeyPrivateKey;

	// msaAccessToken comes from DeviceCodeAuth.pollForAccessToken() - see Client#initialize
	public List<String> getOnlineChainData(String msaAccessToken) throws Exception {
		Gson gson = FunnelMC.instance.fileManagement.normalGson;

		KeyPair ecdsa256KeyPair = Auth.createKeyPair();//for xbox live, xbox live requests use, ES256, ECDSA256
		this.publicKey = (ECPublicKey) ecdsa256KeyPair.getPublic();
		this.privateKey = (ECPrivateKey) ecdsa256KeyPair.getPrivate();
		this.xboxLiveKeyPublicKey = this.publicKey;
		this.xboxLiveKeyPrivateKey = this.privateKey;

		this.xbox = new Xbox(msaAccessToken);
		this.userToken = xbox.getUserToken(this.publicKey, this.privateKey);
		this.deviceToken = xbox.getDeviceToken(this.publicKey, this.privateKey);
		this.titleToken = xbox.getTitleToken(this.publicKey, this.privateKey, deviceToken);
		String xsts = xbox.getXstsToken(userToken, deviceToken, titleToken, this.publicKey, this.privateKey);

		KeyPair ecdsa384KeyPair = EncryptionUtils.createKeyPair();//use ES384, ECDSA384
		this.publicKey = (ECPublicKey) ecdsa384KeyPair.getPublic();
		this.privateKey = (ECPrivateKey) ecdsa384KeyPair.getPrivate();

		/*
		 * So we get a "chain"(json array with info(that has 2 objects)) from minecraft.net using our xsts token
		 * from there we have to add our own chain at the beginning of the chain(json array that minecraft.net sent us),
		 * When is all said and done, we have 3 chains(they are jwt objects, header.payload.signature)
		 * which we send to the server to check
		 */
		String chainData = xbox.requestMinecraftChain(xsts, this.publicKey);
		JsonObject chainDataObject = FunnelMC.instance.fileManagement.jsonParser.parse(chainData).getAsJsonObject();
		JsonArray minecraftNetChain = chainDataObject.get("chain").getAsJsonArray();
		String firstChainHeader = minecraftNetChain.get(0).getAsString();
		firstChainHeader = firstChainHeader.split("\\.")[0];//get the jwt header(base64)
		firstChainHeader = new String(Base64.getDecoder().decode(firstChainHeader.getBytes()));//decode the jwt base64 header
		String firstKeyx5u = FunnelMC.instance.fileManagement.jsonParser.parse(firstChainHeader).getAsJsonObject().get("x5u").getAsString();

		JsonObject newFirstChain = new JsonObject();
		newFirstChain.addProperty("certificateAuthority", true);
		newFirstChain.addProperty("exp", Instant.now().getEpochSecond() + TimeUnit.HOURS.toSeconds(6));
		newFirstChain.addProperty("identityPublicKey", firstKeyx5u);
		newFirstChain.addProperty("nbf", Instant.now().getEpochSecond() - TimeUnit.HOURS.toSeconds(6));

		{
			String publicKeyBase64 = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
			JsonObject jwtHeader = new JsonObject();
			jwtHeader.addProperty("alg", "ES384");
			jwtHeader.addProperty("x5u", publicKeyBase64);

			String header = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(jwtHeader).getBytes());
			String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(newFirstChain).getBytes());

			byte[] dataToSign = (header + "." + payload).getBytes();
			String signatureString = this.signBytes(dataToSign);

			String jwt = header + "." + payload + "." + signatureString;

			chainDataObject.add("chain", this.addChainToBeginning(jwt, minecraftNetChain));//replace the chain with our new chain
		}

		{
			//we are now going to get some data from a chain minecraft sent us(the last chain)
			String lastChain = minecraftNetChain.get(minecraftNetChain.size() - 1).getAsString();
			String lastChainPayload = lastChain.split("\\.")[1];//get the middle(payload) jwt thing
			lastChainPayload = new String(Base64.getDecoder().decode(lastChainPayload.getBytes()));//decode the base64

			JsonObject payloadObject = FunnelMC.instance.fileManagement.jsonParser.parse(lastChainPayload).getAsJsonObject();
			JsonObject extraData = payloadObject.get("extraData").getAsJsonObject();
			this.xuid = extraData.get("XUID").getAsString();
			this.identity = UUID.fromString(extraData.get("identity").getAsString());
			this.displayName = extraData.get("displayName").getAsString();
		}

		List<String> chain = new ArrayList<>();
		for (JsonElement jsonElement : chainDataObject.get("chain").getAsJsonArray()) {
			chain.add(jsonElement.getAsString());
		}
		return chain;
	}

	// Requests a second XSTS token scoped for general Xbox Live APIs (friends, presence,
	// multiplayer session directory) instead of logging into Minecraft, reusing the
	// user/device/title tokens obtained by getOnlineChainData() rather than redoing that exchange.
	// Must be called after getOnlineChainData().
	public String getXboxLiveAuthorizationHeader() throws Exception {
		return this.getXboxTokenForRelyingParty("http://xboxlive.com");
	}

	// Same idea as getXboxLiveAuthorizationHeader(), but for an arbitrary relying party - needed for
	// PlayFab login (NetherNet's MCToken chain), which requires an XSTS token scoped specifically to
	// "rp://playfabapi.com/" rather than the general Xbox Live one. Must be called after
	// getOnlineChainData().
	public String getXboxTokenForRelyingParty(String relyingParty) throws Exception {
		String xsts = this.xbox.getXstsToken(this.userToken, this.deviceToken, this.titleToken, this.xboxLiveKeyPublicKey, this.xboxLiveKeyPrivateKey, relyingParty);
		// The "prv" (privileges) and "agg" (age group) claims here explain permission-flavored MPSD
		// errors ("must have the multiplayer privilege...") that plain 403 bodies don't - logging
		// them lets that be confirmed/ruled out from evidence instead of guessed at.
		try {
			JsonObject xui = FunnelMC.instance.fileManagement.jsonParser.parse(xsts).getAsJsonObject()
					.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject();
			LogManager.getLogger(Auth.class).warn("[FriendsDiag] XSTS DisplayClaims.xui[0] for relyingParty={}: {}", relyingParty, xui);
		} catch (Exception e) {
			LogManager.getLogger(Auth.class).warn("[FriendsDiag] Failed to log XSTS DisplayClaims for relyingParty={}", relyingParty, e);
		}
		return Xbox.buildAuthorizationHeader(xsts);
	}

	public List<String> getOfflineChainData(String username) throws Exception {
		//So we need to assign the a uuid from a username, or else everytime we join a server with the same name, we will get reset(as if we are a new player)
		//Java does it this way, I'm not sure if bedrock does but it gets our goal accomplished, PlayerEntity.getOfflinePlayerUuid
		UUID offlineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
		String xuid = Long.toString(offlineUUID.getLeastSignificantBits());

		Gson gson = FunnelMC.instance.fileManagement.normalGson;
		//KeyPair ecdsa256KeyPair = Auth.createKeyPair();//for xbox live, xbox live requests use, ES256, ECDSA256
		KeyPair ecdsa256KeyPair = EncryptionUtils.createKeyPair();
		this.publicKey = (ECPublicKey) ecdsa256KeyPair.getPublic();
		this.privateKey = (ECPrivateKey) ecdsa256KeyPair.getPrivate();

		String publicKeyBase64 = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());

		JsonObject chain = new JsonObject();//jwtPayload
		//chain.addProperty("certificateAuthority", true);
		chain.addProperty("exp", Instant.now().getEpochSecond() + TimeUnit.HOURS.toSeconds(6));
		chain.addProperty("identityPublicKey", publicKeyBase64);
		chain.addProperty("nbf", Instant.now().getEpochSecond() - TimeUnit.HOURS.toSeconds(6));

		JsonObject extraData = new JsonObject();
		extraData.addProperty("titleId", "896928775"); // Use the Title ID for Windows 10, as some servers ban for an incorrect Title ID.
		extraData.addProperty("identity", offlineUUID.toString());
		extraData.addProperty("displayName", username);
		extraData.addProperty("XUID", xuid);
		chain.add("extraData", extraData);

		JsonObject jwtHeader = new JsonObject();
		jwtHeader.addProperty("alg", "ES384");
		jwtHeader.addProperty("x5u", publicKeyBase64);

		String header = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(jwtHeader).getBytes());
		String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(gson.toJson(chain).getBytes());

		byte[] dataToSign = (header + "." + payload).getBytes();
		String signatureString = this.signBytes(dataToSign);

		String jwt = header + "." + payload + "." + signatureString;

		this.xuid = xuid;
		this.identity = offlineUUID;
		this.displayName = username;

		List<String> chainList = new ArrayList<>();
		chainList.add(jwt);
		return chainList;
	}

	public String signBytes(byte[] dataToSign) throws Exception {
		Signature signature = Signature.getInstance("SHA384withECDSA");
		signature.initSign(this.privateKey);
		signature.update(dataToSign);
		byte[] signatureBytes = JoseStuff.DERToJOSE(signature.sign(), JoseStuff.AlgorithmType.ECDSA384);

		return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
	}

	//thanks gson for not adding a add at index method :pensive:
	private JsonArray addChainToBeginning(String chain, JsonArray chainArray) {
		JsonArray newArray = new JsonArray();
		newArray.add(chain);

		for (JsonElement jsonElement : chainArray) {
			newArray.add(jsonElement);
		}
		return newArray;
	}

	public ECPublicKey getPublicKey() {
		return this.publicKey;
	}

	public ECPrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public String getXuid() {
		return this.xuid;
	}

	public UUID getIdentity() {
		return this.identity;
	}

	public String getDisplayName() {
		return this.displayName;
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
