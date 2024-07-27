package dev.lazurite.toolbox.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

/**
 * Utility class for dealing with {@link ChunkSectionPos}.
 * @since 1.2.7
 */
public class SectionPosUtil {

    public static int posToSectionCoord(double coord) {
        return SectionPosUtil.blockToSectionCoord(BlockPosUtil.posToBlockCoord(coord));
    }

    public static int blockToSectionCoord(int coord) {
        return coord >> 4;
    }

    public static ChunkSectionPos of(int x, int y, int z) {
        return ChunkSectionPos.from(x, y, z);
    }

    public static ChunkSectionPos of(double x, double y, double z) {
        return ChunkSectionPos.from(
                SectionPosUtil.posToSectionCoord(x),
                SectionPosUtil.posToSectionCoord(y),
                SectionPosUtil.posToSectionCoord(z)
        );
    }

    public static ChunkSectionPos of(Vec3d pos) {
        return SectionPosUtil.of(
                SectionPosUtil.posToSectionCoord(pos.getX()),
                SectionPosUtil.posToSectionCoord(pos.getY()),
                SectionPosUtil.posToSectionCoord(pos.getZ())
        );
    }

    public static ChunkSectionPos of(Entity entity) {
        return SectionPosUtil.of(entity.getPos());
    }

    protected SectionPosUtil() { }

}
