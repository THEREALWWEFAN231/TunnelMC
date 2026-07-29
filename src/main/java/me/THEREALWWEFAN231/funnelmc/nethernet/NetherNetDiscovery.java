package me.THEREALWWEFAN231.funnelmc.nethernet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;

// Mirrors gophertunnel's minecraft/service.Discover(): a single GET that returns, per Minecraft
// service (auth, signaling, ...), the base URI and config that service's client should use. Real
// Bedrock clients call this once rather than hardcoding these URIs, since Microsoft can (and does)
// move them between regions/environments.
public class NetherNetDiscovery {

	private static final Logger logger = LogManager.getLogger(NetherNetDiscovery.class);

	private static final String DISCOVERY_URL = "https://client.discovery.minecraft-services.net/api/v1.0/discovery/MinecraftPE/builds/";

	public static class AuthEnvironment {
		public String serviceUri;
		public String issuer;
		public String playFabTitleId;
	}

	public static class SignalingEnvironment {
		public String serviceUri;
		public String stunUri;
		public String turnUri;
	}

	public static class DiscoveryResult {
		public AuthEnvironment auth;
		public SignalingEnvironment signaling;
	}

	public static DiscoveryResult discover(String gameVersion) throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(URI.create(DISCOVERY_URL + gameVersion))
				.header("Content-Type", "application/json")
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		logger.warn("[NetherNetDiag] Discovery raw response: HTTP {} {}", response.statusCode(), response.body());
		if (response.statusCode() != 200) {
			throw new RuntimeException("Discovery failed with HTTP " + response.statusCode() + ": " + response.body());
		}

		// Wrapped as {"result": {...}}, not the {"data": {...}} envelope PlayFab-adjacent endpoints
		// use (confirmed by curling this endpoint directly - it's public and needs no auth).
		JsonObject body = FunnelMC.instance.fileManagement.jsonParser.parse(response.body()).getAsJsonObject();
		JsonObject data = body.getAsJsonObject("result");
		JsonObject serviceEnvironments = data.getAsJsonObject("serviceEnvironments");

		DiscoveryResult result = new DiscoveryResult();

		JsonObject authProd = serviceEnvironments.getAsJsonObject("auth").getAsJsonObject("prod");
		result.auth = new AuthEnvironment();
		result.auth.serviceUri = authProd.get("serviceUri").getAsString();
		result.auth.issuer = authProd.has("issuer") ? authProd.get("issuer").getAsString() : null;
		// Lowercase 'f' - "playfabTitleId", not "playFabTitleId" (confirmed against the real response;
		// the Go struct's field name doesn't necessarily match the actual JSON tag on the wire).
		result.auth.playFabTitleId = authProd.get("playfabTitleId").getAsString();

		JsonObject signalingProd = serviceEnvironments.getAsJsonObject("signaling").getAsJsonObject("prod");
		result.signaling = new SignalingEnvironment();
		result.signaling.serviceUri = signalingProd.get("serviceUri").getAsString();
		result.signaling.stunUri = signalingProd.has("stunUri") ? signalingProd.get("stunUri").getAsString() : null;
		result.signaling.turnUri = signalingProd.has("turnUri") ? signalingProd.get("turnUri").getAsString() : null;

		return result;
	}

}
