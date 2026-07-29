package me.THEREALWWEFAN231.funnelmc.auth;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.JsonObject;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import net.minecraft.client.Minecraft;

// Persists the Microsoft OAuth refresh_token to disk so the player only has to go through the device
// code screen once - every launch after that can silently trade it for a fresh access_token instead
// (see DeviceCodeAuth#refreshAccessToken and MicrosoftLoginScreen's startup check). Stored in plaintext
// like every other Minecraft launcher/mod stores its own MSA tokens (e.g. the vanilla launcher's own
// launcher_accounts.json) - it's the player's own credential living on their own machine.
public class AuthTokenStore {

	private static File file() {
		return new File(Minecraft.getInstance().gameDirectory, "config/funnelmc/auth.json");
	}

	public static String loadRefreshToken() {
		File file = file();
		if (!file.exists()) {
			return null;
		}

		try (FileReader reader = new FileReader(file)) {
			JsonObject json = FunnelMC.instance.fileManagement.jsonParser.parse(reader).getAsJsonObject();
			return json.has("refresh_token") ? json.get("refresh_token").getAsString() : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void saveRefreshToken(String refreshToken) {
		if (refreshToken == null) {
			return;
		}

		File file = file();
		file.getParentFile().mkdirs();

		JsonObject json = new JsonObject();
		json.addProperty("refresh_token", refreshToken);

		try (FileWriter writer = new FileWriter(file)) {
			FunnelMC.instance.fileManagement.normalGson.toJson(json, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
