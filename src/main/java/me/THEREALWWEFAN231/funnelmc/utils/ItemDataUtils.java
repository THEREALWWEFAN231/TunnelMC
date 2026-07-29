package me.THEREALWWEFAN231.funnelmc.utils;

import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;

public class ItemDataUtils {
	
	public static ItemData copyWithCount(ItemData toCopy, int newCount) {
		//probably should find a copy to copy the tag?
		ItemData copied = toCopy.toBuilder().count(newCount).build();
		
		return copied;
	}

}
