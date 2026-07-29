package me.THEREALWWEFAN231.funnelmc.translator.container.screenhandler;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches.container.BedrockContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class ScreenHandlerTranslator<T extends AbstractContainerMenu> {
	
	public abstract BedrockContainer getBedrockContainerFromJava(T javaContainer, int javaSlotId);
	
	public int getJavaSlotFromBedrockContainer(AbstractContainerMenu javaContainer, BedrockContainer bedrockContainer, int bedrockSlotId) {//javaContainer may not be necessary?
		return bedrockSlotId;
	}
	
	public abstract int getBedrockSlotFromJavaContainer(T javaContainer, int javaSlotId, BedrockContainer bedrockContainer);//bedrockContainer may not be necessary?
	
	public abstract Class<? extends AbstractContainerMenu> getScreenHandlerClass();//i could use reflection but it generally wouldn't be ideal
}
