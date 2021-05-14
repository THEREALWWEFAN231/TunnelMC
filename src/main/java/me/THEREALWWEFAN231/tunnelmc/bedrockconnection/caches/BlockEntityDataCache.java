package me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches;

import java.util.HashMap;

import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NbtMap;

public class BlockEntityDataCache {
	
	private HashMap<Vector3i, NbtMap> cachedBlockPositionsData = new HashMap<Vector3i, NbtMap>();

	public HashMap<Vector3i, NbtMap> getCachedBlockPositionsData() {
		return this.cachedBlockPositionsData;
	}
	
	public NbtMap getDataFromBlockPosition(Vector3i blockPosition) {
		return this.cachedBlockPositionsData.get(blockPosition);
	}
	
}
