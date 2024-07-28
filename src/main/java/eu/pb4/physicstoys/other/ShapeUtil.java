package eu.pb4.physicstoys.other;

import eu.pb4.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.IdentityHashMap;

public class ShapeUtil {
    private static final IdentityHashMap<BlockState, MinecraftShape.Convex> BLOCK_SHAPE_MAP = new IdentityHashMap<>();
    public static final MinecraftShape.Convex CUBE = MinecraftShape.convex(new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));
    public static final MinecraftShape.Convex FLAT_ITEM = MinecraftShape.convex(new Box(-0.25, -0.25, -0.1, 0.25, 0.25, 0.1));

    public static MinecraftShape.Convex getBlockShape(BlockState state, World world, BlockPos pos) {
        if (state.getBlock().hasDynamicBounds()) {
            return createBlockShape(state, world);
        }

        var shape = BLOCK_SHAPE_MAP.get(state);
        if (shape == null) {
            shape = createBlockShape(state, world);
            BLOCK_SHAPE_MAP.put(state, shape);
        }
        return shape;
    }

    private static MinecraftShape.Convex createBlockShape(BlockState state, World world) {
        VoxelShape box = state.getCollisionShape(world, BlockPos.ORIGIN);
        if (box.isEmpty()) {
            box = state.getOutlineShape(world, BlockPos.ORIGIN);
        }

        var shape = MinecraftShape.convex(box);
        shape.setScale(0.9f);
        return shape;
    }
}
