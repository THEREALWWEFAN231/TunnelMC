package me.THEREALWWEFAN231.funnelmc.nethernet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;

// Ports JustTalDevelops/playfab's LoginWithXbox (a working, tested Go implementation of exactly this
// call) - the request shape here (headers, InfoRequestParameters, SDK/User-Agent strings) matches
// what the real Minecraft client sends, since PlayFab's API is picky about clients it doesn't
// recognize as legitimate SDKs.
public class PlayFabAuth {

	private static final Logger logger = LogManager.getLogger(PlayFabAuth.class);

	private static final String SDK = "XPlatCppSdk-3.6.190304";
	private static final String USER_AGENT = "libhttpclient/1.0.0.0";

	public static class LoginResult {
		public String sessionTicket;
		public String playFabId;
	}

	// xboxTokenHeader must already be in "XBL3.0 x=<uhs>;<token>" format, using an XSTS token
	// requested for the "rp://playfabapi.com/" relying party specifically - PlayFab rejects XSTS
	// tokens scoped for any other relying party (e.g. the Minecraft login or general Xbox Live ones
	// already used elsewhere in this mod).
	public static LoginResult login(String playFabTitleId, String xboxTokenHeader) throws Exception {
		JsonObject infoRequestParameters = new JsonObject();
		infoRequestParameters.addProperty("GetPlayerProfile", true);
		infoRequestParameters.addProperty("GetUserAccountInfo", true);

		JsonObject body = new JsonObject();
		body.addProperty("CreateAccount", true);
		body.add("InfoRequestParameters", infoRequestParameters);
		body.addProperty("TitleId", playFabTitleId.toUpperCase());
		body.addProperty("XboxToken", xboxTokenHeader);

		String url = "https://" + playFabTitleId.toLowerCase() + ".playfabapi.com/Client/LoginWithXbox?sdk=" + SDK;

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("Accept", "application/json")
				.header("Content-Type", "application/json; charset=utf-8")
				.header("User-Agent", USER_AGENT)
				.header("X-PlayFabSDK", SDK)
				.header("X-ReportErrorAsSuccess", "true")
				.POST(HttpRequest.BodyPublishers.ofString(FunnelMC.instance.fileManagement.normalGson.toJson(body)))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		logger.warn("[NetherNetDiag] PlayFab LoginWithXbox raw response: HTTP {} {}", response.statusCode(), response.body());
		if (response.statusCode() != 200) {
			throw new RuntimeException("PlayFab LoginWithXbox failed with HTTP " + response.statusCode() + ": " + response.body());
		}

		JsonObject responseBody = FunnelMC.instance.fileManagement.jsonParser.parse(response.body()).getAsJsonObject();
		JsonObject data = responseBody.has("data") ? responseBody.getAsJsonObject("data") : null;
		if (data == null || !data.has("SessionTicket")) {
			throw new RuntimeException("PlayFab LoginWithXbox response missing SessionTicket: " + response.body());
		}

		LoginResult result = new LoginResult();
		result.sessionTicket = data.get("SessionTicket").getAsString();
		result.playFabId = data.has("PlayFabId") ? data.get("PlayFabId").getAsString() : null;
		return result;
	}

}
