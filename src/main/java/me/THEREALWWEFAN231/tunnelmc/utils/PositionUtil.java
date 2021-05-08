package me.THEREALWWEFAN231.tunnelmc.utils;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;

public class PositionUtil {
    public static BlockPos toBlockPos(Vector3i vector) {
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    public static BlockPos toBlockPos(Vector3f vector) {
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector3i toBedrockVector3i(BlockPos blockPos) {
        return Vector3i.from(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static Vector3f toBedrockVector3f(BlockPos blockPos) {
        return Vector3f.from(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}
