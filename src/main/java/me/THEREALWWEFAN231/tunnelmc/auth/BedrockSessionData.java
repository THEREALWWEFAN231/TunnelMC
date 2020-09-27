package me.THEREALWWEFAN231.tunnelmc.auth;

import java.util.UUID;

public class BedrockSessionData {
	
	private String xuid;
	private UUID identity;
	private String displayName;
	
	public BedrockSessionData(String xuid, UUID identity, String displayName) {
		this.xuid = xuid;
		this.identity = identity;
		this.displayName = displayName;
	}

	public String getXuid() {
		return xuid;
	}

	public UUID getIdentity() {
		return identity;
	}

	public String getDisplayName() {
		return displayName;
	}

}
