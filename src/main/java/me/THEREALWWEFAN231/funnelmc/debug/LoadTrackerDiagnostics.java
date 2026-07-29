package me.THEREALWWEFAN231.funnelmc.debug;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;

// Temporary instrumentation for the "stuck on Loading Terrain" investigation - reflectively dumps
// ClientPacketListener's private levelLoadTracker state so we can see which of LevelLoadTracker's
// three states (WaitingForServer / WaitingForPlayerChunk / ClientLevelReady) the vanilla dismissal
// logic is actually stuck in, instead of guessing from outside.
//
// Deliberately hooked to Fabric API's END_CLIENT_TICK rather than this mod's own EventPlayerTick:
// EventPlayerTick is fired from a mixin into LocalPlayer.tick(), but that method's entire body -
// including the injection point - is gated behind `this.connection.hasClientLoaded()`, which is
// only ever set true by the exact "level ready" transition this diagnostic exists to observe. Using
// EventPlayerTick here would create the mod's own chicken-and-egg deadlock and never fire at all.
//
// Remove once the stuck-loading-screen issue is resolved.
public class LoadTrackerDiagnostics {

	private static final Logger logger = LogManager.getLogger(LoadTrackerDiagnostics.class);

	private int tickCounter;

	public LoadTrackerDiagnostics() {
		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> this.onClientTick());
	}

	private void onClientTick() {
		this.tickCounter++;
		if (this.tickCounter % 20 != 0) {
			return;
		}
		logState("periodic");
	}

	// Also called directly from StartGameTranslator right after it sends LEVEL_CHUNKS_LOAD_START, to
	// see whether the WaitingForServer -> WaitingForPlayerChunk transition happens synchronously as
	// vanilla's own handleGameEvent() code implies it should, or whether it never happens at all.
	public static void logState(String context) {
		try {
			ClientPacketListener connection = FunnelMC.mc.getConnection();
			if (connection == null) {
				logger.warn("[LoadTrackerDiag:{}] Minecraft.getConnection() is null", context);
				return;
			}

			Field levelLoadTrackerField = ClientPacketListener.class.getDeclaredField("levelLoadTracker");
			levelLoadTrackerField.setAccessible(true);
			Object levelLoadTracker = levelLoadTrackerField.get(connection);

			if (levelLoadTracker == null) {
				logger.warn("[LoadTrackerDiag:{}] levelLoadTracker is null (already cleared - level should be ready)", context);
				return;
			}

			Field clientStateField = levelLoadTracker.getClass().getDeclaredField("clientState");
			clientStateField.setAccessible(true);
			Object clientState = clientStateField.get(levelLoadTracker);

			if (clientState == null) {
				logger.warn("[LoadTrackerDiag:{}] clientState is null", context);
				return;
			}

			String stateClassName = clientState.getClass().getSimpleName();

			if (stateClassName.contains("WaitingForPlayerChunk")) {
				Field playerSectionReadyField = clientState.getClass().getDeclaredField("playerSectionReady");
				playerSectionReadyField.setAccessible(true);
				Object playerSectionReady = playerSectionReadyField.get(clientState);
				logger.warn("[LoadTrackerDiag:{}] state=WaitingForPlayerChunk playerSectionReady={} playerPos={} cameraBlockPos={}",
						context,
						playerSectionReady,
						FunnelMC.mc.player != null ? FunnelMC.mc.player.blockPosition() : "null",
						FunnelMC.mc.gameRenderer != null ? FunnelMC.mc.gameRenderer.mainCamera().blockPosition() : "null");
			} else {
				logger.warn("[LoadTrackerDiag:{}] state={} playerPos={}", context, stateClassName,
						FunnelMC.mc.player != null ? FunnelMC.mc.player.blockPosition() : "null");
			}
		} catch (Exception e) {
			logger.error("[LoadTrackerDiag:{}] Failed to inspect levelLoadTracker", context, e);
		}
	}

}
