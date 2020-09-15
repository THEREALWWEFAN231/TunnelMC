package me.THEREALWWEFAN231.tunnelmc.translator.blockstate;

import java.util.HashMap;
import java.util.Map;

public class BedrockBlockState {

	public String identifier;
	//this holds the block states properties, for example, stone_type, granite
	public HashMap<String, String> properties = new HashMap<String, String>();

	public BedrockBlockState() {

	}

	public void parseFromString(String string) {

		int firstLeftBracketIndex = string.indexOf("[");
		if (firstLeftBracketIndex != -1) {//if its found
			this.identifier = string.substring(0, firstLeftBracketIndex);
		} else {
			this.identifier = string;
		}

		if (firstLeftBracketIndex != -1) {
			String blockProperties = string.substring(firstLeftBracketIndex + 1, string.length() - 1);

			String[] blockProperyKeysAndValues = blockProperties.split(",");

			for (String keyAndValue : blockProperyKeysAndValues) {
				String[] keyAndValueArray = keyAndValue.split("=");

				this.properties.put(keyAndValueArray[0], keyAndValueArray[1]);
			}
		}

	}

	@Override
	public String toString() {
		if (this.identifier == null) {
			return null;
		}

		String string = "";

		string += this.identifier;
		string += "[";

		for (Map.Entry<String, String> entry : this.properties.entrySet()) {
			string += entry.getKey() + "=" + entry.getValue() + ",";
		}
		string = string.substring(0, string.length() - 1);//remove the last comma

		string += "]";

		return string;
	}

}
