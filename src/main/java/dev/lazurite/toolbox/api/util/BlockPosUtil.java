package dev.lazurite.toolbox.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Utility class for dealing with {@link BlockPos}.
 * @since 1.2.7
 */
public class BlockPosUtil {

    public static int posToBlockCoord(double dCoord) {
        final var iCoord = (int) dCoord;
        return dCoord < (double) iCoord ? iCoord - 1 : iCoord;
    }

    public static BlockPos of(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    public static BlockPos of(double x, double y, double z) {
        return new BlockPos(
                BlockPosUtil.posToBlockCoord(x),
                BlockPosUtil.posToBlockCoord(y),
                BlockPosUtil.posToBlockCoord(z)
        );
    }

    public static BlockPos of(Vec3d pos) {
        return BlockPosUtil.of(
                BlockPosUtil.posToBlockCoord(pos.getX()),
                BlockPosUtil.posToBlockCoord(pos.getY()),
                BlockPosUtil.posToBlockCoord(pos.getZ())
        );
    }

    public static BlockPos of(Entity entity) {
        return BlockPosUtil.of(entity.getPos());
    }

    protected BlockPosUtil() { }

}
