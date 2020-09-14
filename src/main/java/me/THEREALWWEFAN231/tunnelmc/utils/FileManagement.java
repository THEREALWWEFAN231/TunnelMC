package me.THEREALWWEFAN231.tunnelmc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class FileManagement {
	
	public Gson normalGson = new GsonBuilder().create();
	public Gson formattedGson = new GsonBuilder().setPrettyPrinting().create();
	public JsonParser jsonParser = new JsonParser();
	
	public String getTextFromInputStream(InputStream inputStream) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String textInFile = "";

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			textInFile += line;
		}
		bufferedReader.close();
		
		return textInFile;
	}
	
	public String getTextFromFile(File file) throws Exception {
		return new String(Files.readAllBytes(file.toPath()));//this is "super fast"
	}
	
}
