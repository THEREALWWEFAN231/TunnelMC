package me.THEREALWWEFAN231.tunnelmc.translator.packet.world;

import com.google.common.collect.Sets;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.packet.LevelEventPacket;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.THEREALWWEFAN231.tunnelmc.mixins.interfaces.IMixinClientPlayerInteractionManager;
import me.THEREALWWEFAN231.tunnelmc.mixins.interfaces.IMixinWorldRenderer;
import me.THEREALWWEFAN231.tunnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.tunnelmc.utils.PositionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.Random;
import java.util.SortedSet;

public class LevelEventTranslator extends PacketTranslator<LevelEventPacket> {
    public static final Object2ObjectMap<Vector3i, BlockBreakingWrapper> BLOCK_BREAKING_INFOS = new Object2ObjectOpenHashMap<>();
    public static final LongSet TO_REMOVE = new LongOpenHashSet();
    private final Random random = new Random();

    @Override
    public void translate(LevelEventPacket packet) {
        if (MinecraftClient.getInstance().world == null) {
            return;
        }

        switch (packet.getType()) {
            case BLOCK_START_BREAK: {
                Vector3i position = packet.getPosition().toInt();
                BlockBreakingInfo blockBreakingInfo = new BlockBreakingInfo(0, PositionUtil.toBlockPos(position));
                BlockBreakingWrapper blockBreakingWrapper = new BlockBreakingWrapper(packet.getData(), blockBreakingInfo);
                BLOCK_BREAKING_INFOS.put(position, blockBreakingWrapper);
                SortedSet<BlockBreakingInfo> sortedSet = Sets.newTreeSet();
                sortedSet.add(blockBreakingInfo);
                ((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer).getBlockBreakingProgressions().put(
                        BlockPos.asLong(position.getX(), position.getY(), position.getZ()), sortedSet);
                break;
            }
            case BLOCK_UPDATE_BREAK: {
                BlockBreakingWrapper blockBreakingWrapper = BLOCK_BREAKING_INFOS.get(packet.getPosition().toInt());
                if (blockBreakingWrapper == null) {
                    break;
                }
                blockBreakingWrapper.length = packet.getData();
                break;
            }
            case BLOCK_STOP_BREAK: {
                if (packet.getPosition().equals(Vector3f.ZERO)) { // Apparently...
                    if (BLOCK_BREAKING_INFOS.containsKey(packet.getPosition().toInt())) {
                        long key = ((IMixinClientPlayerInteractionManager) MinecraftClient.getInstance().interactionManager).getCurrentBreakingPos().asLong();
                        TO_REMOVE.add(key);
                    }
                } else {
                    Vector3i position = packet.getPosition().toInt();
                    if (BLOCK_BREAKING_INFOS.containsKey(position)) {
                        long key = BlockPos.asLong(position.getX(), position.getY(), position.getZ());
                        TO_REMOVE.add(key);
                    }
                }
                break;
            }
            case PARTICLE_CRACK_BLOCK: {
                Direction direction = Direction.byId(packet.getData() >> 24);
                int bedrockRuntimeId = packet.getData() & 0xffffff; // Strip out the above encoding
                BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(bedrockRuntimeId);
                Vector3i vector = packet.getPosition().toInt();
                BlockPos pos = PositionUtil.toBlockPos(vector);
                // Copying most of the code from ParticleManager#addBlockBreakingParticles
                // So we depend on the runtime ID of this packet and not the block at that position
                int i = vector.getX();
                int j = vector.getY();
                int k = vector.getZ();
                Box box = blockState.getOutlineShape(MinecraftClient.getInstance().world, pos).getBoundingBox();
                double d = (double)i + this.random.nextDouble() * (box.maxX - box.minX - 0.20000000298023224D) + 0.10000000149011612D + box.minX;
                double e = (double)j + this.random.nextDouble() * (box.maxY - box.minY - 0.20000000298023224D) + 0.10000000149011612D + box.minY;
                double g = (double)k + this.random.nextDouble() * (box.maxZ - box.minZ - 0.20000000298023224D) + 0.10000000149011612D + box.minZ;
                if (direction == Direction.DOWN) {
                    e = (double)j + box.minY - 0.10000000149011612D;
                }

                if (direction == Direction.UP) {
                    e = (double)j + box.maxY + 0.10000000149011612D;
                }

                if (direction == Direction.NORTH) {
                    g = (double)k + box.minZ - 0.10000000149011612D;
                }

                if (direction == Direction.SOUTH) {
                    g = (double)k + box.maxZ + 0.10000000149011612D;
                }

                if (direction == Direction.WEST) {
                    d = (double)i + box.minX - 0.10000000149011612D;
                }

                if (direction == Direction.EAST) {
                    d = (double)i + box.maxX + 0.10000000149011612D;
                }

                MinecraftClient.getInstance().particleManager.addParticle((new BlockDustParticle(MinecraftClient.getInstance().world,
                        d, e, g, 0.0D, 0.0D, 0.0D, blockState)).setBlockPos(pos).move(0.2F).scale(0.6F));
                break;
            }
            case PARTICLE_DESTROY_BLOCK: {
                MinecraftClient.getInstance().world.syncWorldEvent(MinecraftClient.getInstance().player, 2001,
                        PositionUtil.toBlockPos(packet.getPosition().toInt()),
                        Block.getRawIdFromState(BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getData())));
                break;
            }
        }
    }

    @Override
    public Class<?> getPacketClass() {
        return LevelEventPacket.class;
    }

    public static class BlockBreakingWrapper {
        public long lastUpdate;
        public int length;
        public float currentDuration;
        public BlockBreakingInfo blockBreakingInfo;

        public BlockBreakingWrapper(int length, BlockBreakingInfo blockBreakingInfo) {
            this.length = length;
            this.blockBreakingInfo = blockBreakingInfo;
        }
    }
}
