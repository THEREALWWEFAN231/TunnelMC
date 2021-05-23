package me.THEREALWWEFAN231.tunnelmc.auth;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.primitives.Longs;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;

/**
 * Referenced from the resource below:
 * https://github.com/Sandertv/gophertunnel/tree/master/minecraft/auth
 */
public class Xbox {

	/**
	 * To generate your own access token (for now), login at the following link:
	 * https://login.live.com/oauth20_authorize.srf?client_id=00000000441cc96b&redirect_uri=https://login.live.com/oauth20_desktop.srf&response_type=token&display=touch&scope=service::user.auth.xboxlive.com::MBI_SSL&locale=en
	 * When you're redirected to the blank page, check the URL parameters. Look for the one that says access token, and then set
	 * the "XBOX_ACCESS_TOKEN" environment variable to your access token.
	 *
	 * This will be changed and simplified later on.
	 */
	private final String accessToken;

	private static final String xboxUserAuthURL = "https://user.auth.xboxlive.com/user/authenticate";
	private static final String xboxAuthorizeURL = "https://xsts.auth.xboxlive.com/xsts/authorize";
	private static final String xboxDeviceAuthURL = "https://device.auth.xboxlive.com/device/authenticate";
	private static final String xboxTitleAuthURL = "https://title.auth.xboxlive.com/title/authenticate";
	private static final String minecraftAuthURL = "https://multiplayer.minecraft.net/authentication";

	private final JsonParser jsonParser = TunnelMC.instance.fileManagement.jsonParser;

	public Xbox(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserToken(ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
		jsonObject.addProperty("TokenType", "JWT");

		JsonObject properties = new JsonObject();
		jsonObject.add("Properties", properties);
		properties.addProperty("AuthMethod", "RPS");
		properties.addProperty("SiteName", "user.auth.xboxlive.com");
		properties.addProperty("RpsTicket", "t=" + this.accessToken);

		JsonObject proofKey = new JsonObject();
		properties.add("ProofKey", proofKey);
		proofKey.addProperty("crv", "P-256");
		proofKey.addProperty("alg", "ES256");
		proofKey.addProperty("use", "sig");
		proofKey.addProperty("kty", "EC");
		proofKey.addProperty("x", this.getProofKeyX(publicKey));
		proofKey.addProperty("y", this.getProofKeyY(publicKey));

		URL url = new URL(xboxUserAuthURL);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("x-xbl-contract-version", "1");
		this.addSignatureHeader(connection, jsonObject, privateKey);

		this.writeJsonObjectToPost(connection, jsonObject);

		String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
		JsonObject responceJsonObject = this.jsonParser.parse(responce).getAsJsonObject();

		return responceJsonObject.get("Token").getAsString();
	}

	public String getDeviceToken(ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
		jsonObject.addProperty("TokenType", "JWT");

		JsonObject properties = new JsonObject();
		jsonObject.add("Properties", properties);
		properties.addProperty("AuthMethod", "ProofOfPossession");
		properties.addProperty("DeviceType", "Nintendo");
		properties.addProperty("Id", UUID.randomUUID().toString());
		properties.addProperty("SerialNumber", UUID.randomUUID().toString());
		properties.addProperty("Version", "0.0.0.0");

		JsonObject proofKey = new JsonObject();
		properties.add("ProofKey", proofKey);
		proofKey.addProperty("crv", "P-256");
		proofKey.addProperty("alg", "ES256");
		proofKey.addProperty("use", "sig");
		proofKey.addProperty("kty", "EC");
		proofKey.addProperty("x", this.getProofKeyX(publicKey));
		proofKey.addProperty("y", this.getProofKeyY(publicKey));

		URL url = new URL(xboxDeviceAuthURL);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("x-xbl-contract-version", "1");
		this.addSignatureHeader(connection, jsonObject, privateKey);

		this.writeJsonObjectToPost(connection, jsonObject);

		String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
		JsonObject responceJsonObject = this.jsonParser.parse(responce).getAsJsonObject();

		return responceJsonObject.get("Token").getAsString();
	}

	public String getTitleToken(ECPublicKey publicKey, ECPrivateKey privateKey, String deviceToken) throws Exception {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
		jsonObject.addProperty("TokenType", "JWT");

		JsonObject properties = new JsonObject();
		jsonObject.add("Properties", properties);
		properties.addProperty("AuthMethod", "RPS");
		properties.addProperty("DeviceToken", deviceToken);
		properties.addProperty("SiteName", "user.auth.xboxlive.com");
		properties.addProperty("RpsTicket", "t=" + this.accessToken);

		JsonObject proofKey = new JsonObject();
		properties.add("ProofKey", proofKey);
		proofKey.addProperty("crv", "P-256");
		proofKey.addProperty("alg", "ES256");
		proofKey.addProperty("use", "sig");
		proofKey.addProperty("kty", "EC");
		proofKey.addProperty("x", this.getProofKeyX(publicKey));
		proofKey.addProperty("y", this.getProofKeyY(publicKey));

		URL url = new URL(xboxTitleAuthURL);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("x-xbl-contract-version", "1");
		this.addSignatureHeader(connection, jsonObject, privateKey);

		this.writeJsonObjectToPost(connection, jsonObject);

		String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
		JsonObject responceJsonObject = this.jsonParser.parse(responce).getAsJsonObject();

		return responceJsonObject.get("Token").getAsString();
	}

	public String getXstsToken(String userToken, String deviceToken, String titleToken, ECPublicKey publicKey, ECPrivateKey privateKey) throws Exception {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("RelyingParty", "https://multiplayer.minecraft.net/");
		jsonObject.addProperty("TokenType", "JWT");

		JsonObject properties = new JsonObject();
		jsonObject.add("Properties", properties);

		JsonArray userTokens = new JsonArray();
		userTokens.add(userToken);

		properties.addProperty("DeviceToken", deviceToken);
		properties.addProperty("TitleToken", titleToken);
		properties.add("UserTokens", userTokens);
		properties.addProperty("SandboxId", "RETAIL");

		JsonObject proofKey = new JsonObject();
		properties.add("ProofKey", proofKey);
		proofKey.addProperty("crv", "P-256");
		proofKey.addProperty("alg", "ES256");
		proofKey.addProperty("use", "sig");
		proofKey.addProperty("kty", "EC");
		proofKey.addProperty("x", this.getProofKeyX(publicKey));
		proofKey.addProperty("y", this.getProofKeyY(publicKey));

		URL url = new URL(xboxAuthorizeURL);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		connection.setRequestProperty("x-xbl-contract-version", "1");
		this.addSignatureHeader(connection, jsonObject, privateKey);

		this.writeJsonObjectToPost(connection, jsonObject);

		return TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
	}

	public String requestMinecraftChain(String xsts, ECPublicKey publicKey) throws Exception {
		JsonObject xstsObject = this.jsonParser.parse(xsts).getAsJsonObject();

		String pubKeyData = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("identityPublicKey", pubKeyData);

		URL url = new URL(minecraftAuthURL);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization", "XBL3.0 x=" + xstsObject.get("DisplayClaims").getAsJsonObject().getAsJsonArray("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString() + ";" + xstsObject.get("Token").getAsString());
		connection.setRequestProperty("User-Agent", "MCPE/UWP");
		connection.setRequestProperty("Client-Version", Client.instance.bedrockClient.getSession().getPacketCodec().getMinecraftVersion());

		this.writeJsonObjectToPost(connection, jsonObject);

		return TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
	}

	private void writeJsonObjectToPost(HttpsURLConnection connection, JsonObject jsonObject) throws Exception {
		connection.setDoOutput(true);

		DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
		dataOutputStream.writeBytes(TunnelMC.instance.fileManagement.gJson.toJson(jsonObject));
		dataOutputStream.flush();
	}

	private String getProofKeyX(ECPublicKey ecPublicKey) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(Xbox.bigIntegerToByteArray(ecPublicKey.getW().getAffineX()));
	}

	private String getProofKeyY(ECPublicKey ecPublicKey) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(Xbox.bigIntegerToByteArray(ecPublicKey.getW().getAffineY()));
	}

	private void addSignatureHeader(HttpsURLConnection httpsURLConnection, JsonObject postData, ECPrivateKey privateKey) throws Exception {
		long currentTime = this.windowsTimestamp();
		ByteArrayOutputStream bytesToSign = new ByteArrayOutputStream();

		bytesToSign.write(new byte[] { 0, 0, 0, 1, 0 });
		bytesToSign.write(Longs.toByteArray(currentTime));
		bytesToSign.write(new byte[] { 0 });

		bytesToSign.write("POST".getBytes());
		bytesToSign.write(new byte[] { 0 });
		String query = httpsURLConnection.getURL().getQuery();
		if (query == null) {
			query = "";
		}
		bytesToSign.write((httpsURLConnection.getURL().getPath() + query).getBytes());
		bytesToSign.write(new byte[] { 0 });
		String authorization = httpsURLConnection.getRequestProperty("Authorization");
		if (authorization == null) {
			authorization = "";
		}
		bytesToSign.write(authorization.getBytes());
		bytesToSign.write(new byte[] { 0 });
		bytesToSign.write(TunnelMC.instance.fileManagement.gJson.toJson(postData).getBytes());
		bytesToSign.write(new byte[] { 0 });

		Signature signature = Signature.getInstance("SHA256withECDSA");
		signature.initSign(privateKey);
		signature.update(bytesToSign.toByteArray());
		byte[] signatureBytes = JoseStuff.DERToJOSE(signature.sign(), JoseStuff.AlgorithmType.ECDSA256);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(new byte[] { 0, 0, 0, 1 });
		byteArrayOutputStream.write(Longs.toByteArray(currentTime));
		byteArrayOutputStream.write(signatureBytes);
		httpsURLConnection.addRequestProperty("Signature", Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
	}

	// windowsTimestamp returns a Windows specific timestamp. It has a certain offset from Unix time which must be accounted for.
	private long windowsTimestamp() {
		return (Instant.now().getEpochSecond() + 11644473600L) * 10000000L;
	}

	//so sometimes getAffineX/Y toByteArray returns 33 or 31(really rare) bytes we are suppose to get 32 bytes, as said in these stackoverflows, they basically say if byte 0 is 0(33 bytes?) we can remove it
	//https://stackoverflow.com/questions/57379134/bouncy-castle-ecc-key-pair-generation-produces-different-sizes-for-the-coordinat
	//https://stackoverflow.com/questions/4407779/biginteger-to-byte
	private static byte[] bigIntegerToByteArray(BigInteger bigInteger) {
		byte[] array = bigInteger.toByteArray();
		if (array[0] == 0) {
			byte[] newArray = new byte[array.length - 1];
			System.arraycopy(array, 1, newArray, 0, newArray.length);
			return newArray;
		}
		return array;
	}

}
