package me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler;

import java.util.HashMap;

import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainer;
import me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler.translators.GenericContainerScreenHandlerTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler.translators.PlayerScreenHandlerTranslator;
import net.minecraft.screen.ScreenHandler;

public class ScreenHandlerTranslatorManager {
	
	private static final HashMap<Class<? extends ScreenHandler>, ScreenHandlerTranslator<?>> REGISTRY = new HashMap<Class<? extends ScreenHandler>, ScreenHandlerTranslator<?>>();
	
	public static void load() {
		ScreenHandlerTranslatorManager.add(new PlayerScreenHandlerTranslator());
		ScreenHandlerTranslatorManager.add(new GenericContainerScreenHandlerTranslator());
	}
	
	private static void add(ScreenHandlerTranslator<?> translator) {
		ScreenHandlerTranslatorManager.REGISTRY.put(translator.getScreenHandlerClass(), translator);
	}
	
	private static ScreenHandlerTranslator<ScreenHandler> getTranslator(ScreenHandler screenHandler){
		Class<? extends ScreenHandler> screenHandlerClass = screenHandler.getClass();
		ScreenHandlerTranslator<ScreenHandler> translator = (ScreenHandlerTranslator<ScreenHandler>) ScreenHandlerTranslatorManager.REGISTRY.get(screenHandlerClass);
		
		if(translator == null) {
			System.out.println("No screen handler found for " + screenHandlerClass);
			return null;
		}
		
		return translator;
	}
	
	public static BedrockContainer getBedrockContainerFromJava(ScreenHandler javaContainer, int javaSlotId) {
		return ScreenHandlerTranslatorManager.getTranslator(javaContainer).getBedrockContainerFromJava(javaContainer, javaSlotId);
	}
	
	public static int getJavaSlotFromBedrockContainer(ScreenHandler javaContainer, BedrockContainer bedrockContainer, int bedrockSlotId) {
		return ScreenHandlerTranslatorManager.getTranslator(javaContainer).getJavaSlotFromBedrockContainer(javaContainer, bedrockContainer, bedrockSlotId);
	}
	
	public static int getBedrockSlotFromJavaContainer(ScreenHandler javaContainer, int javaSlotId, BedrockContainer bedrockContainer) {
		return ScreenHandlerTranslatorManager.getTranslator(javaContainer).getBedrockSlotFromJavaContainer(javaContainer, javaSlotId, bedrockContainer);
	}
}
