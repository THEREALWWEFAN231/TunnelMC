package me.THEREALWWEFAN231.funnelmc.translator.packet.world;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.THEREALWWEFAN231.funnelmc.FunnelMC;
import me.THEREALWWEFAN231.funnelmc.events.EventPlayerTick;
import me.THEREALWWEFAN231.funnelmc.translator.PacketTranslator;
import me.THEREALWWEFAN231.funnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.funnelmc.utils.PositionUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Random;

// Block-breaking progress tracking moved from WorldRenderer to ClientLevel#destroyBlockProgress
// in modern MC - see MixinWorldRenderer, which cancels the vanilla path so we can drive it from
// Bedrock's own timing here instead of the client's own break-speed prediction.
public class LevelEventTranslator extends PacketTranslator<LevelEventPacket> {
    public static final Object2ObjectMap<Vector3i, BlockBreakingWrapper> BLOCK_BREAKING_INFOS = new Object2ObjectOpenHashMap<>();
    public static final LongSet TO_REMOVE = new LongOpenHashSet();
    private final Random random = new Random();

    public LevelEventTranslator() {
        EventManager.register(this);
    }

    @EventTarget
    public void onPlayerTick(EventPlayerTick event) {
        if (Minecraft.getInstance().level == null || BLOCK_BREAKING_INFOS.isEmpty()) {
            return;
        }

        var iterator = BLOCK_BREAKING_INFOS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Vector3i position = entry.getKey();
            BlockBreakingWrapper wrapper = entry.getValue();

            if ((System.currentTimeMillis() - wrapper.lastUpdate) >= 50) {
                wrapper.currentDuration += (wrapper.length / (float) 65535);
                wrapper.lastUpdate = System.currentTimeMillis();
            }
            int stage = (int) (wrapper.currentDuration * 10F) - 1;
            stage = Math.min(Math.max(stage, 0), 10);
            long key = BlockPos.asLong(position.getX(), position.getY(), position.getZ());
            int id = (int) key;

            if (TO_REMOVE.remove(key) || stage >= 10) {
                iterator.remove();
                Minecraft.getInstance().level.destroyBlockProgress(id, PositionUtil.toBlockPos(position), -1);
                continue;
            }
            Minecraft.getInstance().level.destroyBlockProgress(id, PositionUtil.toBlockPos(position), stage);
        }
    }

    @Override
    public void translate(LevelEventPacket packet) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        if (!(packet.getType() instanceof LevelEvent levelEvent)) {
            return;
        }

        switch (levelEvent) {
            case BLOCK_START_BREAK: {
                Vector3i position = packet.getPosition().toInt();
                BlockBreakingWrapper blockBreakingWrapper = new BlockBreakingWrapper(packet.getData());
                BLOCK_BREAKING_INFOS.put(position, blockBreakingWrapper);
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
                    for (Vector3i position : BLOCK_BREAKING_INFOS.keySet()) {
                        TO_REMOVE.add(BlockPos.asLong(position.getX(), position.getY(), position.getZ()));
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
                Direction direction = Direction.from3DDataValue(packet.getData() >> 24);
                int bedrockRuntimeId = packet.getData() & 0xffffff; // Strip out the above encoding
                BlockState blockState = BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(bedrockRuntimeId);
                Vector3i vector = packet.getPosition().toInt();
                BlockPos pos = PositionUtil.toBlockPos(vector);
                // Copying most of the code from ParticleEngine's block breaking particle logic
                // So we depend on the runtime ID of this packet and not the block at that position
                int i = vector.getX();
                int j = vector.getY();
                int k = vector.getZ();
                AABB box = blockState.getShape(Minecraft.getInstance().level, pos).bounds();
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

                Minecraft.getInstance().particleEngine.add(new TerrainParticle(Minecraft.getInstance().level,
                        d, e, g, 0.0D, 0.0D, 0.0D, blockState, pos));
                break;
            }
            case PARTICLE_DESTROY_BLOCK: {
                Minecraft.getInstance().level.levelEvent(Minecraft.getInstance().player, 2001,
                        PositionUtil.toBlockPos(packet.getPosition().toInt()),
                        Block.getId(BlockPaletteTranslator.RUNTIME_ID_TO_BLOCK_STATE.get(packet.getData())));
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

        public BlockBreakingWrapper(int length) {
            this.length = length;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
}
