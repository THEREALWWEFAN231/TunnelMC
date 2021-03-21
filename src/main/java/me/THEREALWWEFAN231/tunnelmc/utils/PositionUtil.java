package me.THEREALWWEFAN231.tunnelmc.utils;

import com.nukkitx.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;

public class PositionUtil {
    public static BlockPos vector3iToBlockPos(Vector3i vector) {
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }
}
