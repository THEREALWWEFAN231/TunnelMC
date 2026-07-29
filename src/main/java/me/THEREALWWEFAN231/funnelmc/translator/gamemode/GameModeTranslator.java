package me.THEREALWWEFAN231.funnelmc.translator.gamemode;

import org.cloudburstmc.protocol.bedrock.data.GameType;

public class GameModeTranslator {

	public static net.minecraft.world.level.GameType bedrockToJava(GameType gameType, GameType worldDefaultGameType) {
		switch (gameType) {
			case SURVIVAL:
			case SURVIVAL_VIEWER:
			case DEFAULT:
				return net.minecraft.world.level.GameType.SURVIVAL;
			case CREATIVE:
			case CREATIVE_VIEWER:
				return net.minecraft.world.level.GameType.CREATIVE;
			case ADVENTURE:
				return net.minecraft.world.level.GameType.ADVENTURE;
			case SPECTATOR:
				return net.minecraft.world.level.GameType.SPECTATOR;
		}

		return null;
	}

}
