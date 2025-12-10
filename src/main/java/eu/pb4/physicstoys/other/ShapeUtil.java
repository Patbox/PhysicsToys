package eu.pb4.physicstoys.other;

import eu.pb4.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import java.util.IdentityHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapeUtil {
    private static final IdentityHashMap<BlockState, MinecraftShape.Convex> BLOCK_SHAPE_MAP = new IdentityHashMap<>();
    public static final MinecraftShape.Convex CUBE = MinecraftShape.convex(new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));
    public static final MinecraftShape.Convex FLAT_ITEM = MinecraftShape.convex(new AABB(-0.25, -0.25, -0.1, 0.25, 0.25, 0.1));

    public static MinecraftShape.Convex getBlockShape(BlockState state, Level world, BlockPos pos) {
        if (state.getBlock().hasDynamicShape()) {
            return createBlockShape(state, world);
        }

        var shape = BLOCK_SHAPE_MAP.get(state);
        if (shape == null) {
            shape = createBlockShape(state, world);
            BLOCK_SHAPE_MAP.put(state, shape);
        }
        return shape;
    }

    private static MinecraftShape.Convex createBlockShape(BlockState state, Level world) {
        VoxelShape box = state.getCollisionShape(world, BlockPos.ZERO);
        if (box.isEmpty()) {
            box = state.getShape(world, BlockPos.ZERO);
        }

        var shape = MinecraftShape.convex(box);
        shape.setScale(0.9f);
        return shape;
    }
}
