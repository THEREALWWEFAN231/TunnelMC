package me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.containers;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainers;

public class PlayerInventoryContainer extends BedrockContainer {

	public static final int SIZE = 36;

	public PlayerInventoryContainer() {
		super(PlayerInventoryContainer.SIZE, BedrockContainers.PLAYER_INVENTORY_COTNAINER_ID);
	}
	
	@Override
	public int convertJavaSlotIdToBedrockSlotId(int javaSlotId) {
		
		if(javaSlotId >= 36) {//if it's a java hotbar slot 36->44
			return javaSlotId - 36;//convert to bedrock slot, 0-8
		}
		
		//this check *isn't* needed *if* we are in the correct container, which we should be, for now I'm keeping this if statement, and return 0 for debugging purposes
		if(javaSlotId >= 9 && javaSlotId <= 35) {
			return javaSlotId;//java main inventory, the 27 slots have the same id on bedrock
		}
		
		return 0;
	}

}
