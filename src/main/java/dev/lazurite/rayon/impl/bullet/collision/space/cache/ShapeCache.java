package dev.lazurite.rayon.impl.bullet.collision.space.cache;

import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.IdentityHashMap;

public final class ShapeCache {
    private static final MinecraftShape FALLBACK_SHAPE = MinecraftShape.convex(new Box(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f));

    private static final IdentityHashMap<BlockState, MinecraftShape> SHAPES_SERVER = new IdentityHashMap<>();

    public static MinecraftShape getShapeFor(BlockState blockState, World level, BlockPos blockPos) {
        if (blockState.getBlock().hasDynamicBounds()) {
            return createShapeFor(blockState, level, blockPos);
        }

        var shape = SHAPES_SERVER.get(blockState);

        if (shape == null) {
            shape = createShapeFor(blockState, level, BlockPos.ORIGIN);
            SHAPES_SERVER.put(blockState, shape);
        }

        return shape;
    }

    private static MinecraftShape createShapeFor(BlockState blockState, World level, BlockPos blockPos) {
        final var voxelShape = blockState.getCollisionShape(level, blockPos);
        if (!voxelShape.isEmpty()) {
            return MinecraftShape.convex(voxelShape);
        } else {
            return FALLBACK_SHAPE;
        }
    }
}
