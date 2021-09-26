package me.THEREALWWEFAN231.tunnelmc.mixins;

import com.nukkitx.math.vector.Vector3i;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.Client;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.world.LevelEventTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.SortedSet;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Shadow @Final private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "setBlockBreakingInfo", at = @At("HEAD"), cancellable = true)
    public void cancelBlockBreakingInfo(int entityId, BlockPos pos, int stage, CallbackInfo ci) {
        if (Client.instance.isConnectionOpen() && entityId == this.client.player.getId()) {
            // Don't let the client set this - let the server
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        // Manually set the block breaking progressions based on the server
        ObjectIterator<Map.Entry<Vector3i, LevelEventTranslator.BlockBreakingWrapper>> iterator = LevelEventTranslator.BLOCK_BREAKING_INFOS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Vector3i, LevelEventTranslator.BlockBreakingWrapper> entry = iterator.next();

            if ((System.currentTimeMillis() - entry.getValue().lastUpdate) >= 50) {
                entry.getValue().currentDuration += (entry.getValue().length / (float) 65535);
                entry.getValue().lastUpdate = System.currentTimeMillis();
            }
            int z = (int) (entry.getValue().currentDuration * 10F) - 1;
            z = MathHelper.clamp(z, 0, 10);
            long key = BlockPos.asLong(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
            if (LevelEventTranslator.TO_REMOVE.remove(key) || z == 10) { // Prevent concurrency issues by having a separate list
                // We have exceeded the stages
                iterator.remove();
                this.blockBreakingProgressions.remove(key);
                continue;
            }
            entry.getValue().blockBreakingInfo.setStage(z);
        }
    }
}
