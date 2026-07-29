package me.THEREALWWEFAN231.funnelmc.nethernet;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCDataChannelInit;
import dev.onvoid.webrtc.RTCDataChannelObserver;
import dev.onvoid.webrtc.RTCDataChannelState;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceConnectionState;
import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.RTCOfferOptions;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;

// Drives an actual WebRTC connection over an already-authenticated SignalingConnection, per
// df-mc/nethernet-spec: we act as the ICE controller (the connecting side always does per the
// spec), offer two data channels ("ReliableDataChannel" ordered, "UnreliableDataChannel"
// unordered) before creating the SDP offer (data channels must exist before offer generation to
// appear in the SDP at all), send that offer as a CONNECTREQUEST signal, then handle the
// CONNECTRESPONSE answer and trickled CANDIDATEADD signals as they arrive.
//
// webrtc-java wraps the real C++ WebRTC implementation, so its own RTCIceCandidate.sdp format
// already matches what the spec calls "the format used by the C++ implementation of WebRTC" - no
// manual candidate string formatting is needed here, unlike go-nethernet (which reimplements
// WebRTC's ICE/SDP logic in pure Go and has to hand-format that string itself).
public class NetherNetTransport {

	private static final Logger logger = LogManager.getLogger(NetherNetTransport.class);

	private final PeerConnectionFactory factory;
	private final RTCPeerConnection peerConnection;
	private final SignalingConnection signaling;
	private final String targetNetworkId;
	private final long connectionId;

	private RTCDataChannel reliableChannel;
	private RTCDataChannel unreliableChannel;

	private final CompletableFuture<Void> dataChannelOpen = new CompletableFuture<>();

	public NetherNetTransport(SignalingConnection signaling, long targetNetherNetId, String turnCredentialsJson) {
		this.signaling = signaling;
		this.targetNetworkId = Long.toUnsignedString(targetNetherNetId);
		// Kept non-negative (masked off the sign bit) purely so it round-trips cleanly through
		// NetherNetSignal's decimal encode/decode without needing unsigned-aware parsing on our
		// own connection ID specifically - the remote's IDs are handled as unsigned regardless.
		this.connectionId = new SecureRandom().nextLong() & Long.MAX_VALUE;

		RTCConfiguration config = new RTCConfiguration();
		config.iceServers = parseIceServers(turnCredentialsJson);

		this.factory = new PeerConnectionFactory();
		this.peerConnection = this.factory.createPeerConnection(config, new PeerConnectionObserver() {
			@Override
			public void onIceCandidate(RTCIceCandidate candidate) {
				logger.warn("[NetherNetDiag] Local ICE candidate gathered: {}", candidate.sdp);
				NetherNetSignal signal = new NetherNetSignal(NetherNetSignal.TYPE_CANDIDATE, NetherNetTransport.this.connectionId, candidate.sdp);
				NetherNetTransport.this.signaling.sendSignal(signal, NetherNetTransport.this.targetNetworkId);
			}

			@Override
			public void onIceConnectionChange(RTCIceConnectionState state) {
				logger.warn("[NetherNetDiag] ICE connection state: {}", state);
			}

			@Override
			public void onConnectionChange(RTCPeerConnectionState state) {
				logger.warn("[NetherNetDiag] Peer connection state: {}", state);
			}

			@Override
			public void onDataChannel(RTCDataChannel dataChannel) {
				// The connecting side (us) is the one that creates both data channels up front and
				// offers them in the SDP - the spec says the server never opens its own, so this
				// firing at all would mean something unexpected happened.
				logger.warn("[NetherNetDiag] Unexpected inbound data channel from remote: {}", dataChannel.getLabel());
			}
		});
	}

	private static List<RTCIceServer> parseIceServers(String turnCredentialsJson) {
		JsonObject credentials = FunnelMC.instance.fileManagement.jsonParser.parse(turnCredentialsJson).getAsJsonObject();
		JsonArray turnAuthServers = credentials.getAsJsonArray("TurnAuthServers");

		List<RTCIceServer> iceServers = new ArrayList<>();
		for (JsonElement element : turnAuthServers) {
			JsonObject server = element.getAsJsonObject();
			RTCIceServer iceServer = new RTCIceServer();
			iceServer.username = server.get("Username").getAsString();
			iceServer.password = server.get("Password").getAsString();
			for (JsonElement url : server.getAsJsonArray("Urls")) {
				iceServer.urls.add(url.getAsString());
			}
			iceServers.add(iceServer);
		}
		return iceServers;
	}

	// Initiates the connection: creates the two data channels, generates and sends the SDP offer,
	// then relies on the caller forwarding every Signal received from targetNetworkId into
	// handleSignal() (trickled ICE candidates are sent automatically via onIceCandidate above as
	// they're gathered - no separate call needed for our own outgoing candidates).
	public void connect() {
		RTCDataChannelInit reliableInit = new RTCDataChannelInit();
		reliableInit.ordered = true;
		this.reliableChannel = this.peerConnection.createDataChannel("ReliableDataChannel", reliableInit);
		this.registerChannelObserver(this.reliableChannel, "ReliableDataChannel");

		RTCDataChannelInit unreliableInit = new RTCDataChannelInit();
		unreliableInit.ordered = false;
		unreliableInit.maxRetransmits = 0;
		this.unreliableChannel = this.peerConnection.createDataChannel("UnreliableDataChannel", unreliableInit);
		this.registerChannelObserver(this.unreliableChannel, "UnreliableDataChannel");

		this.peerConnection.createOffer(new RTCOfferOptions(), new CreateSessionDescriptionObserver() {
			@Override
			public void onSuccess(RTCSessionDescription description) {
				NetherNetTransport.this.peerConnection.setLocalDescription(description, new SetSessionDescriptionObserver() {
					@Override
					public void onSuccess() {
						logger.warn("[NetherNetDiag] Local offer set, sending CONNECTREQUEST to {}", NetherNetTransport.this.targetNetworkId);
						NetherNetSignal signal = new NetherNetSignal(NetherNetSignal.TYPE_OFFER, NetherNetTransport.this.connectionId, description.sdp);
						NetherNetTransport.this.signaling.sendSignal(signal, NetherNetTransport.this.targetNetworkId);
					}

					@Override
					public void onFailure(String error) {
						logger.error("[NetherNetDiag] setLocalDescription (offer) failed: {}", error);
						NetherNetTransport.this.dataChannelOpen.completeExceptionally(new RuntimeException("setLocalDescription failed: " + error));
					}
				});
			}

			@Override
			public void onFailure(String error) {
				logger.error("[NetherNetDiag] createOffer failed: {}", error);
				NetherNetTransport.this.dataChannelOpen.completeExceptionally(new RuntimeException("createOffer failed: " + error));
			}
		});
	}

	private void registerChannelObserver(RTCDataChannel channel, String name) {
		channel.registerObserver(new RTCDataChannelObserver() {
			@Override
			public void onBufferedAmountChange(long previousAmount) {
			}

			@Override
			public void onStateChange() {
				RTCDataChannelState state = channel.getState();
				logger.warn("[NetherNetDiag] {} state changed to {}", name, state);
				if (state == RTCDataChannelState.OPEN && !NetherNetTransport.this.dataChannelOpen.isDone()) {
					NetherNetTransport.this.dataChannelOpen.complete(null);
				}
			}

			@Override
			public void onMessage(RTCDataChannelBuffer buffer) {
				byte[] data = new byte[buffer.data.remaining()];
				buffer.data.get(data);
				logger.warn("[NetherNetDiag] {} received {} bytes (packet pipeline not wired up yet)", name, data.length);
			}
		});
	}

	public void handleSignal(NetherNetSignal signal) {
		switch (signal.type) {
			case NetherNetSignal.TYPE_ANSWER -> {
				logger.warn("[NetherNetDiag] Received CONNECTRESPONSE, applying remote answer");
				RTCSessionDescription answer = new RTCSessionDescription(RTCSdpType.ANSWER, signal.data);
				this.peerConnection.setRemoteDescription(answer, new SetSessionDescriptionObserver() {
					@Override
					public void onSuccess() {
						logger.warn("[NetherNetDiag] Remote answer applied successfully");
					}

					@Override
					public void onFailure(String error) {
						logger.error("[NetherNetDiag] setRemoteDescription (answer) failed: {}", error);
						NetherNetTransport.this.dataChannelOpen.completeExceptionally(new RuntimeException("setRemoteDescription failed: " + error));
					}
				});
			}
			case NetherNetSignal.TYPE_CANDIDATE -> {
				RTCIceCandidate candidate = new RTCIceCandidate("0", 0, signal.data);
				this.peerConnection.addIceCandidate(candidate);
				logger.warn("[NetherNetDiag] Added remote ICE candidate: {}", signal.data);
			}
			case NetherNetSignal.TYPE_ERROR -> {
				logger.error("[NetherNetDiag] Remote signaled CONNECTERROR code={}", signal.data);
				this.dataChannelOpen.completeExceptionally(new RuntimeException("Remote signaled CONNECTERROR code=" + signal.data));
			}
			default -> logger.warn("[NetherNetDiag] Unhandled NetherNet signal type={}", signal.type);
		}
	}

	public CompletableFuture<Void> whenDataChannelOpen() {
		return this.dataChannelOpen;
	}

	public void close() {
		if (this.reliableChannel != null) {
			this.reliableChannel.close();
		}
		if (this.unreliableChannel != null) {
			this.unreliableChannel.close();
		}
		this.peerConnection.close();
		this.factory.dispose();
	}

}
