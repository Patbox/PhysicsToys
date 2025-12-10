package eu.pb4.rayon.api.util;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Utility class for dealing with {@link SectionPos}.
 * @since 1.2.7
 */
public class SectionPosUtil {

    public static int posToSectionCoord(double coord) {
        return SectionPosUtil.blockToSectionCoord(BlockPosUtil.posToBlockCoord(coord));
    }

    public static int blockToSectionCoord(int coord) {
        return coord >> 4;
    }

    public static SectionPos of(int x, int y, int z) {
        return SectionPos.of(x, y, z);
    }

    public static SectionPos of(double x, double y, double z) {
        return SectionPos.of(
                SectionPosUtil.posToSectionCoord(x),
                SectionPosUtil.posToSectionCoord(y),
                SectionPosUtil.posToSectionCoord(z)
        );
    }

    public static SectionPos of(Vec3 pos) {
        return SectionPosUtil.of(
                SectionPosUtil.posToSectionCoord(pos.x()),
                SectionPosUtil.posToSectionCoord(pos.y()),
                SectionPosUtil.posToSectionCoord(pos.z())
        );
    }

    public static SectionPos of(Entity entity) {
        return SectionPosUtil.of(entity.position());
    }

    protected SectionPosUtil() { }

}
