package me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import net.minecraft.screen.ScreenHandler;

public abstract class ScreenHandlerTranslator<T extends ScreenHandler> {
	
	public abstract BedrockContainer getBedrockContainerFromJava(T javaContainer, int javaSlotId);
	
	public int getJavaSlotFromBedrockContainer(ScreenHandler javaContainer, BedrockContainer bedrockContainer, int bedrockSlotId) {//javaContainer may not be necessary?
		return bedrockSlotId;
	}
	
	public abstract int getBedrockSlotFromJavaContainer(T javaContainer, int javaSlotId, BedrockContainer bedrockContainer);//bedrockContainer may not be necessary?
	
	public abstract Class<? extends ScreenHandler> getScreenHandlerClass();//i could use reflection but it generally wouldn't be ideal
}
