package me.THEREALWWEFAN231.funnelmc.bedrockconnection.caches;

import java.util.HashMap;

import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;

public class BlockEntityDataCache {
	
	private HashMap<Vector3i, NbtMap> cachedBlockPositionsData = new HashMap<Vector3i, NbtMap>();

	public HashMap<Vector3i, NbtMap> getCachedBlockPositionsData() {
		return this.cachedBlockPositionsData;
	}
	
	public NbtMap getDataFromBlockPosition(Vector3i blockPosition) {
		return this.cachedBlockPositionsData.get(blockPosition);
	}
	
}
