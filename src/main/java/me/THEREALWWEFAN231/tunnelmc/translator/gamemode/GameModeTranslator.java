package me.THEREALWWEFAN231.tunnelmc.translator.gamemode;

import com.nukkitx.protocol.bedrock.data.GameType;

import net.minecraft.world.GameMode;

public class GameModeTranslator {//i want to use GeneralTranslator but i have 2 types in the translate method :thinking:

	public static GameMode bedrockToJava(GameType gameType, GameType worldDefaultGameType) {

		//TODO: SURVIVAL_VIEWER/CREATIVE_VIEWER is probably spectator

		if (gameType == GameType.SURVIVAL || gameType == GameType.SURVIVAL_VIEWER || gameType == GameType.DEFAULT) {//not sure about default but who cares
			return GameMode.SURVIVAL;
		} else if (gameType == GameType.CREATIVE || gameType == GameType.CREATIVE_VIEWER) {
			return GameMode.CREATIVE;
		} else if (gameType == GameType.ADVENTURE) {
			return GameMode.ADVENTURE;
		} else if (gameType == GameType.ADVENTURE) {
			return GameMode.ADVENTURE;
		} else if (gameType == GameType.WORLD_DEFAULT) {
			return GameModeTranslator.bedrockToJava(worldDefaultGameType, worldDefaultGameType);
		}

		return GameMode.NOT_SET;
	}

}
