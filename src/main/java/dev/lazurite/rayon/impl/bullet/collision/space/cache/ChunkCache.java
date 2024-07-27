package dev.lazurite.rayon.impl.bullet.collision.space.cache;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.block.BlockProperty;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Used for storing block that can be queried during physics execution.
 * An implementation of this should be updated/reloaded every tick on the
 * main game thread.
 * @see MinecraftSpace#step
 */
public interface ChunkCache {
    static ChunkCache create(MinecraftSpace space) {
        return new SimpleChunkCache(space);
    }

    static boolean isValidBlock(BlockState blockState) {
        if (blockState == null) {
            return false;
        }

        final var block = blockState.getBlock();
        final var properties = BlockProperty.getBlockProperty(block);

        return properties != null ? properties.collidable() :
                !blockState.isAir() &&
                !block.canMobSpawnInside(blockState) && (
                        blockState.getFluidState().isEmpty() || (
                                blockState.contains(Properties.WATERLOGGED) &&
                                blockState.get(Properties.WATERLOGGED)
                        )
                );
    }

    void refreshAll();
    void loadBlockData(BlockPos blockPos);
    void loadFluidData(BlockPos blockPos);

    MinecraftSpace getSpace();
    List<BlockData> getBlockData();
    List<FluidColumn> getFluidColumns();
    Optional<BlockData> getBlockData(BlockPos blockPos);
    Optional<FluidColumn> getFluidColumn(BlockPos blockPos);

    boolean isActive(BlockPos blockPos);

    record BlockData (World level, BlockPos blockPos, BlockState blockState, MinecraftShape shape) { }
    record FluidData (World level, BlockPos blockPos, FluidState fluidState) { }

    class FluidColumn {
        private final FluidData top;
        private final FluidData bottom;
        private final Vector3f flow;
        private final float height;
        private long index;

        public FluidColumn(BlockPos start, World level) {
            this.index = Integer.toUnsignedLong(start.getX()) << 32l | Integer.toUnsignedLong(start.getZ());
            final var cursor = new BlockPos(start).mutableCopy();
            var fluidState = level.getFluidState(cursor);

            // find bottom block
            while (!fluidState.isEmpty()) {
                cursor.set(cursor.down());
                fluidState = level.getFluidState(cursor);
            }

            cursor.set(cursor.up()); // the above loop ends at one below the bottom
            fluidState = level.getFluidState(cursor);
            this.bottom = new FluidData(level, new BlockPos(cursor), level.getFluidState(cursor));

            // find top block
            while (!fluidState.isEmpty()) {
                cursor.set(cursor.up());
                fluidState = level.getFluidState(cursor);
            }

            cursor.set(cursor.down());
            fluidState = level.getFluidState(cursor);

            this.top = new FluidData(level, new BlockPos(cursor), fluidState);
            this.height = fluidState.getHeight(level, cursor);

            // Water flow direction
            this.flow = Convert.toBullet(fluidState.getVelocity(level, cursor));
        }

        public boolean contains(BlockPos blockPos) {
            return top.blockPos.getX() == blockPos.getX()
                    && top.blockPos.getZ() == blockPos.getZ()
                    && top.blockPos.getY() >= blockPos.getY()
                    && bottom.blockPos.getY() <= blockPos.getY();
        }

        public FluidData getTop() {
            return this.top;
        }

        public FluidData getBottom() {
            return this.bottom;
        }

        public float getTopHeight(Vector3f position) {
//            if (flow.lengthSquared() == 0) {
//                return 0.875f;
//            }
//            final var x = position.x;
//            final var z = position.z;
//
//            final var minHeight = 0.125f;
//            final var maxHeight = height;
//
//            final var xhat = flow.dot(new Vector3f(1, 0, 0));
//            final var zhat = flow.dot(new Vector3f(0, 0, 1));
//
//            if (xhat == 0) {
//                if (zhat > 0) {
//                    return (1.0f - z) * maxHeight + z * minHeight;
//                } else if (zhat < 0) {
//                    return (1.0f - z) * minHeight + z * maxHeight;
//                }
//            } else if (zhat == 0) {
//                if (xhat > 0) {
//                    return (1.0f - x) * maxHeight + x * minHeight;
//                } else if (xhat < 0) {
//                    return (1.0f - x) * minHeight + x * maxHeight;
//                }
//            }

//            final var hitResult = topShape.clip(VectorHelper.toVec3(Convert.toMinecraft(position)), new Vec3(top.blockPos.getX() + 0.5f, top.blockPos.getY(), top.blockPos.getZ() + 0.5f), top.blockPos);
//            final var y = topShape.collide(Direction.Axis.Y, new AABB(top.blockPos).move(VectorHelper.toVec3(Convert.toMinecraft(position))), 0.875f);

//            if (hitResult != null) {
//                return position.y - (float) hitResult.getLocation().y;
//            }

            return height;
        }

        public int getHeight() {
            return this.top.blockPos.getY() - this.bottom.blockPos.getY() + 1;
        }

        public Vector3f getFlow() {
            return this.flow;
        }

        public long getIndex() {
            return this.index;
        }
    }
}