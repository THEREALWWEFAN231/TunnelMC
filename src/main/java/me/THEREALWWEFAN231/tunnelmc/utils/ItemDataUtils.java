package me.THEREALWWEFAN231.tunnelmc.utils;

import com.nukkitx.protocol.bedrock.data.inventory.ItemData;

public class ItemDataUtils {
	
	public static ItemData copyWithCount(ItemData toCopy, int newCount) {
		return toCopy.toBuilder().count(newCount).build();
	}

}
