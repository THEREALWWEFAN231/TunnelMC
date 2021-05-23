package me.THEREALWWEFAN231.tunnelmc.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FileManagement {

	public Gson gJson = new GsonBuilder().disableHtmlEscaping().create();
	public JsonParser jsonParser = new JsonParser();

	public String getTextFromInputStream(InputStream inputStream) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, length);
		}

		byteArrayOutputStream.close();
		inputStream.close();

		return byteArrayOutputStream.toString("UTF-8");
	}

	public JsonObject getJsonObjectFromResource(String resourceName) {
		InputStream inputStream = FileManagement.class.getClassLoader().getResourceAsStream(resourceName);
		if (inputStream == null) {
			System.out.println("Resource \"" + resourceName + "\" does not exist!");
			return null;
		}

		try {
			return this.jsonParser.parse(this.getTextFromInputStream(inputStream)).getAsJsonObject();
		} catch (Exception e) {
			System.out.println("Failed to read \"" + resourceName + "\": " + e.getMessage());
			return null;
		}
	}

}