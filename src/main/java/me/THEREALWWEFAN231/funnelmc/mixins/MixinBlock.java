package me.THEREALWWEFAN231.funnelmc.mixins;

import me.THEREALWWEFAN231.funnelmc.bedrockconnection.Client;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Block#onBreak(World, BlockPos, BlockState, PlayerEntity) no longer exists - the closest modern
// equivalent hook is playerWillDestroy, called just before a broken block is removed. It returns
// the BlockState to actually remove (used for multi-part blocks like beds/doors to also clear the
// paired half), so cancelling this by returning the untouched state is a reasonable stand-in for
// the old "skip the block's own break side effects, let the server tell us what happened" intent,
// though it means paired-block cleanup (beds/doors) may not happen client-side. TODO: revisit if
// that turns out to matter in practice.
@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "playerWillDestroy", at = @At("HEAD"), cancellable = true)
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir) {
        if (Client.instance.isConnectionOpen()) {
            cir.setReturnValue(state);
        }
    }
}
