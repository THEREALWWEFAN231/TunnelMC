package me.THEREALWWEFAN231.funnelmc.utils;

import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ScaffoldPlace {

	//so this isn't exactly like bedrock, but for now it gets the job down, but we need to make it so the players cant break blocks, only place(or add a option for this at a later time), which would call for a whole new technique
	public static void setRaycastResult() {
		if (FunnelMC.mc.hitResult.getType() == HitResult.Type.MISS && Mth.wrapDegrees(FunnelMC.mc.player.getXRot()) > 50) {

			BlockPos belowThePlayer = FunnelMC.mc.player.blockPosition().below();
			Block blockUnderThePlayer = FunnelMC.mc.level.getBlockState(belowThePlayer).getBlock();
			if (blockUnderThePlayer instanceof AirBlock) {
				return;
			}

			Direction direction = Direction.fromYRot(FunnelMC.mc.player.getYRot());
			//+0.5 for the middle of the block, because why not
			FunnelMC.mc.hitResult = new BlockHitResult(new Vec3(belowThePlayer.getX() + 0.5, belowThePlayer.getY() + 0.5, belowThePlayer.getZ() + 0.5), direction, belowThePlayer, false);
		}
	}

}
