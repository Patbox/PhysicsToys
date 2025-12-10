package eu.pb4.rayon.impl.bullet.collision.space.cache;

import eu.pb4.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import java.util.IdentityHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ShapeCache {
    private static final MinecraftShape FALLBACK_SHAPE = MinecraftShape.convex(new AABB(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f));

    private static final IdentityHashMap<BlockState, MinecraftShape> SHAPES_SERVER = new IdentityHashMap<>();

    public static MinecraftShape getShapeFor(BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.getBlock().hasDynamicShape()) {
            return createShapeFor(blockState, level, blockPos);
        }

        var shape = SHAPES_SERVER.get(blockState);

        if (shape == null) {
            shape = createShapeFor(blockState, level, BlockPos.ZERO);
            SHAPES_SERVER.put(blockState, shape);
        }

        return shape;
    }

    private static MinecraftShape createShapeFor(BlockState blockState, Level level, BlockPos blockPos) {
        final var voxelShape = blockState.getCollisionShape(level, blockPos);
        if (!voxelShape.isEmpty()) {
            return MinecraftShape.convex(voxelShape);
        } else {
            return FALLBACK_SHAPE;
        }
    }
}
