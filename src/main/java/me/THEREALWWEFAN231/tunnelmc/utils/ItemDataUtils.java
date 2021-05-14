package me.THEREALWWEFAN231.tunnelmc.utils;

import com.nukkitx.protocol.bedrock.data.inventory.ItemData;

public class ItemDataUtils {
	
	public static ItemData copyWithCount(ItemData toCopy, int newCount) {
		//probably should find a copy to copy the tag
		ItemData copied = ItemData.builder().id(toCopy.getId()).damage(toCopy.getDamage()).count(newCount).tag(toCopy.getTag()).canPlace(toCopy.getCanPlace()).canBreak(toCopy.getCanBreak()).blockingTicks(toCopy.getBlockingTicks()).blockRuntimeId(toCopy.getBlockRuntimeId()).usingNetId(toCopy.isUsingNetId()).netId(toCopy.getNetId()).build();
		
		return copied;
	}

}
