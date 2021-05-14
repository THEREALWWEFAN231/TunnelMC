package me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.containers;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainers;

public class PlayerArmorContainer extends BedrockContainer {
	
	public static final int SIZE = 4;

	public PlayerArmorContainer() {
		super(PlayerArmorContainer.SIZE, BedrockContainers.PLAYER_ARMOR_COTNAINER_ID);
	}

}
