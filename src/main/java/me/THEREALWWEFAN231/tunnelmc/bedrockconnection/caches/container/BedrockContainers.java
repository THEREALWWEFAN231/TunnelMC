package me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container;

import java.util.HashMap;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.containers.PlayerArmorContainer;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.containers.PlayerContainerCursorContainer;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.containers.PlayerInventoryContainer;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.containers.PlayerOffhandContainer;

public class BedrockContainers {
	
	public static final int PLAYER_INVENTORY_COTNAINER_ID = 0;
	public static final int PLAYER_OFFHAND_COTNAINER_ID = 119;
	public static final int PLAYER_ARMOR_COTNAINER_ID = 120;
	public static final int PLAYER_CONTAINER_CURSOR_COTNAINER_ID = 124;
	
	private HashMap<Integer, BedrockContainer> containers;
	
	private PlayerInventoryContainer playerInventory;
	private PlayerOffhandContainer playerOffhandContainer;
	private PlayerArmorContainer playerArmorContainer;
	private PlayerContainerCursorContainer playerContainerCursorContainer;
	
	public BedrockContainers() {
		this.containers = new HashMap<Integer, BedrockContainer>();
		
		this.containers.put(BedrockContainers.PLAYER_INVENTORY_COTNAINER_ID, this.playerInventory = new PlayerInventoryContainer());
		this.containers.put(BedrockContainers.PLAYER_OFFHAND_COTNAINER_ID, this.playerOffhandContainer = new PlayerOffhandContainer());
		this.containers.put(BedrockContainers.PLAYER_ARMOR_COTNAINER_ID, this.playerArmorContainer = new PlayerArmorContainer());
		this.containers.put(BedrockContainers.PLAYER_CONTAINER_CURSOR_COTNAINER_ID, this.playerContainerCursorContainer = new PlayerContainerCursorContainer());
	}
	
	public HashMap<Integer, BedrockContainer> getContainers() {
		return this.containers;
	}
	
	public PlayerInventoryContainer getPlayerInventory() {
		return this.playerInventory;
	}

	public PlayerOffhandContainer getPlayerOffhandContainer() {
		return this.playerOffhandContainer;
	}

	public PlayerArmorContainer getPlayerArmorContainer() {
		return this.playerArmorContainer;
	}

	public PlayerContainerCursorContainer getPlayerContainerCursorContainer() {
		return this.playerContainerCursorContainer;
	}

}
