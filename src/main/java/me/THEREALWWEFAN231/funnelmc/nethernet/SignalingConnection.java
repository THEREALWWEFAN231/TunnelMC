package me.THEREALWWEFAN231.funnelmc.nethernet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;

// A persistent NetherNet signaling websocket connection. Unlike the earlier phase-1
// SignalingClient (which only proved the auth chain by waiting for the first Credentials message
// and closing), this stays open for the lifetime of a WebRTC negotiation: sending our SDP
// offer/ICE candidates as Signals and dispatching incoming answer/candidate Signals from the
// remote peer to a Listener.
public class SignalingConnection {

	private static final Logger logger = LogManager.getLogger(SignalingConnection.class);

	public interface Listener {
		void onCredentials(String credentialsJson);

		void onSignal(NetherNetSignal signal, String fromNetworkId);

		void onClosed(int statusCode, String reason);
	}

	private final WebSocket webSocket;
	private final String myNetworkId;
	private final CompletableFuture<String> credentialsFuture = new CompletableFuture<>();

	private SignalingConnection(WebSocket webSocket, String myNetworkId) {
		this.webSocket = webSocket;
		this.myNetworkId = myNetworkId;
	}

	public String getMyNetworkId() {
		return this.myNetworkId;
	}

	public static SignalingConnection connect(String signalingServiceUri, String authorizationHeader, Listener listener) throws Exception {
		String myNetworkId = Long.toUnsignedString(new SecureRandom().nextLong());
		String wsUrl = signalingServiceUri.replaceAll("/+$", "") + "/ws/v1.0/signaling/" + myNetworkId;

		logger.warn("[NetherNetDiag] Opening persistent signaling connection {} (our networkId={})", wsUrl, myNetworkId);

		StringBuilder textBuffer = new StringBuilder();
		SignalingConnection[] selfHolder = new SignalingConnection[1];

		WebSocket.Listener wsListener = new WebSocket.Listener() {
			@Override
			public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
				textBuffer.append(data);
				webSocket.request(1);
				if (!last) {
					return null;
				}

				String message = textBuffer.toString();
				textBuffer.setLength(0);
				logger.warn("[NetherNetDiag] Signaling message received: {}", message);

				try {
					JsonObject json = FunnelMC.instance.fileManagement.jsonParser.parse(message).getAsJsonObject();
					int type = json.get("Type").getAsInt();
					if (type == 2) {
						String credentialsJson = json.get("Message").getAsString();
						if (selfHolder[0] != null && !selfHolder[0].credentialsFuture.isDone()) {
							selfHolder[0].credentialsFuture.complete(credentialsJson);
						}
						listener.onCredentials(credentialsJson);
					} else if (type == 1) {
						String from = json.has("From") ? json.get("From").getAsString() : null;
						NetherNetSignal signal = NetherNetSignal.decode(json.get("Message").getAsString());
						listener.onSignal(signal, from);
					} else {
						logger.warn("[NetherNetDiag] Unhandled signaling message type={}: {}", type, message);
					}
				} catch (Exception e) {
					logger.error("[NetherNetDiag] Failed to handle signaling message: {}", message, e);
				}
				return null;
			}

			@Override
			public void onError(WebSocket webSocket, Throwable error) {
				logger.error("[NetherNetDiag] Signaling websocket error", error);
				if (selfHolder[0] != null) {
					selfHolder[0].credentialsFuture.completeExceptionally(error);
				}
			}

			@Override
			public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
				logger.warn("[NetherNetDiag] Signaling websocket closed: statusCode={} reason={}", statusCode, reason);
				listener.onClosed(statusCode, reason);
				return null;
			}
		};

		HttpClient httpClient = HttpClient.newHttpClient();
		WebSocket webSocket = httpClient.newWebSocketBuilder()
				.header("Authorization", authorizationHeader)
				.buildAsync(URI.create(wsUrl), wsListener)
				.get(15, TimeUnit.SECONDS);

		SignalingConnection connection = new SignalingConnection(webSocket, myNetworkId);
		selfHolder[0] = connection;
		return connection;
	}

	// Blocks (off the calling thread's own budget) until the server's initial Credentials message
	// arrives - it's always the first thing sent after connecting, and NetherNetTransport needs
	// the TURN/STUN servers from it before ICE gathering can start.
	public String awaitCredentials(long timeoutSeconds) throws Exception {
		return this.credentialsFuture.get(timeoutSeconds, TimeUnit.SECONDS);
	}

	public void sendSignal(NetherNetSignal signal, String toNetworkId) {
		JsonObject message = new JsonObject();
		message.addProperty("Type", 1);
		message.addProperty("To", Long.parseUnsignedLong(toNetworkId));
		message.addProperty("Message", signal.encode());
		String json = FunnelMC.instance.fileManagement.normalGson.toJson(message);
		logger.warn("[NetherNetDiag] Sending signal to {}: {}", toNetworkId, signal.type);
		this.webSocket.sendText(json, true);
	}

	public void close() {
		this.webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done");
	}

}
