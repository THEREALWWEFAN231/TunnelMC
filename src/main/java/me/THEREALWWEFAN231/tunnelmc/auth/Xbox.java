package me.THEREALWWEFAN231.tunnelmc.auth;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.primitives.Longs;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;

public class Xbox {

	//go here, log in, and in the redirected url you will have your access token, https://login.live.com/oauth20_authorize.srf?client_id=00000000441cc96b&redirect_uri=https://login.live.com/oauth20_desktop.srf&response_type=token&display=touch&scope=service::user.auth.xboxlive.com::MBI_SSL&locale=en
	private String accessToken = "enter your access token";

	public String xblUserAuthURL = "https://user.auth.xboxlive.com/user/authenticate";
	public String xblAuthorizeURL = "https://xsts.auth.xboxlive.com/xsts/authorize";
	public String xblDeviceAuthURL = "https://device.auth.xboxlive.com/device/authenticate";
	public String xblTitleAuthURL = "https://title.auth.xboxlive.com/title/authenticate";
	public String minecraftAuthURL = "https://multiplayer.minecraft.net/authentication";

	public String userToken(PublicKey publicKey) {

		try {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
			jsonObject.addProperty("TokenType", "JWT");

			JsonObject properties = new JsonObject();
			jsonObject.add("Properties", properties);
			properties.addProperty("AuthMethod", "RPS");
			properties.addProperty("SiteName", "user.auth.xboxlive.com");
			properties.addProperty("RpsTicket", "t=" + this.accessToken);

			/*JsonObject proofKey = new JsonObject();
			properties.add("ProofKey", proofKey);
			proofKey.addProperty("crv", "P-256");
			proofKey.addProperty("alg", "ES256");
			proofKey.addProperty("use", "sig");
			proofKey.addProperty("kty", "EC");
			proofKey.addProperty("x", Base64.getEncoder().encodeToString(ecPublicKey.getW().getAffineX().toByteArray()));
			proofKey.addProperty("y", Base64.getEncoder().encodeToString(ecPublicKey.getW().getAffineY().toByteArray()));*/

			URL url = new URL(this.xblUserAuthURL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("x-xbl-contract-version", "1");
			connection.setDoOutput(true);

			DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
			dataOutputStream.writeBytes(TunnelMC.instance.fileManagement.normalGson.toJson(jsonObject));
			dataOutputStream.flush();

			String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
			JsonObject responceJsonObject = TunnelMC.instance.fileManagement.jsonParser.parse(responce).getAsJsonObject();

			return responceJsonObject.get("Token").getAsString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public String deviceToken(PublicKey publicKey, PrivateKey privateKey) {

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
		proofKey.addProperty("x", Base64.getUrlEncoder().withoutPadding().encodeToString(Xbox.bigIntegerToByteArray(publicKey.point.x)));
		proofKey.addProperty("y", Base64.getUrlEncoder().withoutPadding().encodeToString(Xbox.bigIntegerToByteArray(publicKey.point.y)));

		try {

			URL url = new URL(this.xblDeviceAuthURL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("x-xbl-contract-version", "1");
			this.sign(connection, TunnelMC.instance.fileManagement.normalGson.toJson(jsonObject), privateKey);
			connection.setDoOutput(true);

			DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
			dataOutputStream.writeBytes(TunnelMC.instance.fileManagement.normalGson.toJson(jsonObject));
			dataOutputStream.flush();

			String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
			JsonObject responceJsonObject = TunnelMC.instance.fileManagement.jsonParser.parse(responce).getAsJsonObject();

			return responceJsonObject.get("Token").getAsString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public String titleToken(PublicKey publicKey, PrivateKey privateKey, String deviceToken) {

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
		proofKey.addProperty("x", Base64.getUrlEncoder().withoutPadding().encodeToString(Xbox.bigIntegerToByteArray(publicKey.point.x)));
		proofKey.addProperty("y", Base64.getUrlEncoder().withoutPadding().encodeToString(Xbox.bigIntegerToByteArray(publicKey.point.y)));

		try {

			URL url = new URL(this.xblTitleAuthURL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("x-xbl-contract-version", "1");
			this.sign(connection, TunnelMC.instance.fileManagement.normalGson.toJson(jsonObject), privateKey);
			connection.setDoOutput(true);

			DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
			dataOutputStream.writeBytes(TunnelMC.instance.fileManagement.normalGson.toJson(jsonObject));
			dataOutputStream.flush();

			String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
			JsonObject responceJsonObject = TunnelMC.instance.fileManagement.jsonParser.parse(responce).getAsJsonObject();

			return responceJsonObject.get("Token").getAsString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public JsonObject xstsToken(String userToken, String deviceToken, String titleToken) {

		try {
			JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty("RelyingParty", "https://multiplayer.minecraft.net/");
			jsonObject.addProperty("TokenType", "JWT");

			JsonObject properties = new JsonObject();
			jsonObject.add("properties", properties);

			JsonArray userTokens = new JsonArray();
			userTokens.add(userToken);

			properties.addProperty("DeviceToken", deviceToken);
			properties.addProperty("TitleToken", titleToken);
			properties.add("UserTokens", userTokens);
			properties.addProperty("SandboxId", "RETAIL");

			URL url = new URL(this.xblAuthorizeURL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			connection.setRequestProperty("x-xbl-contract-version", "1");
			connection.setDoOutput(true);

			DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
			dataOutputStream.writeBytes(TunnelMC.instance.fileManagement.normalGson.toJson(jsonObject));
			dataOutputStream.flush();

			String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
			return TunnelMC.instance.fileManagement.jsonParser.parse(responce).getAsJsonObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public String RequestMinecraftChain(JsonObject xsts, PublicKey publicKey) {
		try {
			String pubKeyData = Base64.getEncoder().encodeToString(publicKey.toDer().getBytes());

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("identityPublicKey", pubKeyData);

			URL url = new URL(this.minecraftAuthURL);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "XBL3.0 x=" + xsts.get("DisplayClaims").getAsJsonObject().getAsJsonArray("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString() + ";" + xsts.get("Token").getAsString());
			connection.setRequestProperty("User-Agent", "MCPE/UWP");
			connection.setRequestProperty("Client-Version", /*Client.instance.bedrockClient.getSession().getPacketCodec().getMinecraftVersion()*/ "1.16.20" + "");
			connection.setDoOutput(true);

			DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
			dataOutputStream.writeBytes(TunnelMC.instance.fileManagement.normalGson.toJson(jsonObject));
			dataOutputStream.flush();

			String responce = TunnelMC.instance.fileManagement.getTextFromInputStream(connection.getInputStream());
			return responce;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sign(HttpsURLConnection httpsURLConnection, String body, PrivateKey privateKey) throws Exception {
		long currentTime = this.windowsTimestamp();
		ByteArrayOutputStream hash = new ByteArrayOutputStream();

		hash.write(new byte[] { 0, 0, 0, 1, 0 });
		hash.write(Longs.toByteArray(currentTime));
		hash.write(new byte[] { 0 });

		hash.write("POST".getBytes());
		hash.write(new byte[] { 0 });
		String query = httpsURLConnection.getURL().getQuery();
		if (query == null) {
			query = "";
		}
		hash.write((httpsURLConnection.getURL().getPath() + query).getBytes());
		hash.write(new byte[] { 0 });
		String authorization = httpsURLConnection.getRequestProperty("Authorization");
		if (authorization == null) {
			authorization = "";
		}
		hash.write(authorization.getBytes());
		hash.write(new byte[] { 0 });
		hash.write(body.getBytes("UTF-8"));//TODO: i dont think utf-8 is needed
		hash.write(new byte[] { 0 });

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = digest.digest(hash.toByteArray());
		//Signature signature = Signature.getInstance("SHA512withECDSA");
		//signature.initSign(privateKey);
		//signature.update(hashBytes);
		//byte[] signatureBytes = signature.sign();
		com.starkbank.ellipticcurve.Signature signature = Ecdsa.signTunnelMc(hashBytes, privateKey);
		byte[] signatureBytes = null;
		ByteArrayOutputStream kek = new ByteArrayOutputStream();
		kek.write(bigIntegerToByteArray(signature.r));
		kek.write(bigIntegerToByteArray(signature.s));
		signatureBytes = kek.toByteArray();

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(new byte[] { 0, 0, 0, 1 });
		buf.write(Longs.toByteArray(currentTime));

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(buf.toByteArray());
		byteArrayOutputStream.write(signatureBytes);
		httpsURLConnection.addRequestProperty("Signature", Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
	}

	// windowsTimestamp returns a Windows specific timestamp. It has a certain offset from Unix time which must be
	// accounted for.
	public long windowsTimestamp() {
		return (Instant.now().getEpochSecond() + 11644473600L) * 10000000L;
	}

	//so sometimes getAffineX/Y toByteArray returns 33 or 31(really rare) bytes we are suppose to get 32 bytes, as said in these stackoverflows, they basically say if byte 0 is 0(33 bytes?) we can remove it
	//https://stackoverflow.com/questions/57379134/bouncy-castle-ecc-key-pair-generation-produces-different-sizes-for-the-coordinat
	//https://stackoverflow.com/questions/4407779/biginteger-to-byte
	public static byte[] bigIntegerToByteArray(BigInteger bigInteger) {
		byte[] array = bigInteger.toByteArray();
		if (array[0] == 0) {
			byte[] newArray = new byte[array.length - 1];
			System.arraycopy(array, 1, newArray, 0, newArray.length);
			return newArray;
		}
		return array;
	}

	//https://stackoverflow.com/questions/49974441/extracting-r-s-and-verifying-ecdsa-signature-remotely
	public static BigInteger extractR(byte[] signature) throws Exception {
		int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
		int lengthR = signature[startR + 1];
		return new BigInteger(Arrays.copyOfRange(signature, startR + 2, startR + 2 + lengthR));
	}

	public static BigInteger extractS(byte[] signature) throws Exception {
		int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
		int lengthR = signature[startR + 1];
		int startS = startR + 2 + lengthR;
		int lengthS = signature[startS + 1];
		return new BigInteger(Arrays.copyOfRange(signature, startS + 2, startS + 2 + lengthS));
	}

}
