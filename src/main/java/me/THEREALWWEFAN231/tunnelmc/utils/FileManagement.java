package me.THEREALWWEFAN231.tunnelmc.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileManagement {

	public Gson normalGson = new GsonBuilder().disableHtmlEscaping().create();
	public Gson formattedGson = new GsonBuilder().setPrettyPrinting().create();
	public JsonParser jsonParser = new JsonParser();

	public String getTextFromInputStream(InputStream inputStream) throws Exception {
		//this is "super fast"
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, length);
		}

		byteArrayOutputStream.close();//says it does nothing, but who cares
		inputStream.close();

		return byteArrayOutputStream.toString("UTF-8");
	}

	public String getTextFromFile(File file) throws Exception {
		return new String(Files.readAllBytes(file.toPath()));//this is "super fast"
	}

	public JsonObject getJsonObjectFromResource(String resourceName) {

		InputStream inputStream = FileManagement.class.getClassLoader().getResourceAsStream(resourceName);

		JsonObject jsonObject = null;
		try {
			jsonObject = this.jsonParser.parse(this.getTextFromInputStream(inputStream)).getAsJsonObject();
		} catch (Exception e) {
			System.out.println("Failed to read \"" + resourceName + "\"");
			return null;
		}

		return jsonObject;

	}

}
