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

	//so this isn't exactly like bedrock, but for now it gets the job down, but we need to make it so the players cant break blocks, only place(or add a option for this at a later time), which would call for a whole new technique
	public static void setRaycastResult() {
		if (TunnelMC.mc.crosshairTarget.getType() == HitResult.Type.MISS && MathHelper.wrapDegrees(TunnelMC.mc.player.getPitch()) > 50) {

			BlockPos belowThePlayer = TunnelMC.mc.player.getBlockPos().down();
			Block blockUnderThePlayer = TunnelMC.mc.world.getBlockState(belowThePlayer).getBlock();
			if (blockUnderThePlayer instanceof AirBlock) {
				return;
			}

			Direction direction = Direction.fromRotation(TunnelMC.mc.player.getYaw());
			//+0.5 for the middle of the block, because why not
			TunnelMC.mc.crosshairTarget = new BlockHitResult(new Vec3d(belowThePlayer.getX() + 0.5, belowThePlayer.getY() + 0.5, belowThePlayer.getZ() + 0.5), direction, belowThePlayer, false);
		}
	}

}
