package me.THEREALWWEFAN231.tunnelmc.utils;

import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ScaffoldPlace {

	/**
	 * This isn't exactly like bedrock, but it gets the job done for now.
	 * Later on, we need to make that there's an option to toggle both breaking and placing.
	 */
	public static void setRaycastResult() {
		if (TunnelMC.mc.world == null || TunnelMC.mc.crosshairTarget == null || TunnelMC.mc.player == null) {
			System.out.println("Attempted to set ray cast result with null required fields!");
			return;
		}

		if (TunnelMC.mc.crosshairTarget.getType() == HitResult.Type.MISS && MathHelper.wrapDegrees(TunnelMC.mc.player.pitch) > 50) {
			BlockPos belowThePlayer = TunnelMC.mc.player.getBlockPos().down();
			Block blockUnderThePlayer = TunnelMC.mc.world.getBlockState(belowThePlayer).getBlock();
			if (blockUnderThePlayer instanceof AirBlock) {
				return;
			}

			Direction direction = Direction.fromRotation(TunnelMC.mc.player.yaw);
			TunnelMC.mc.crosshairTarget = new BlockHitResult(new Vec3d(belowThePlayer.getX() + 0.5, belowThePlayer.getY() + 0.5, belowThePlayer.getZ() + 0.5), direction, belowThePlayer, false);
		}
	}

}
