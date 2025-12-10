package eu.pb4.rayon.api.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * Utility class for dealing with {@link ChunkPos}.
 * @since 1.2.7
 */
public class ChunkPosUtil {

    public static int posToChunkCoord(double coord) {
        return ChunkPosUtil.blockToChunkCoord(BlockPosUtil.posToBlockCoord(coord));
    }

    public static int blockToChunkCoord(int coord) {
        return coord >> 4;
    }

    public static ChunkPos of(int x, int z) {
        return new ChunkPos(x, z);
    }

    public static ChunkPos of(double x, double z) {
        return new ChunkPos(
                ChunkPosUtil.posToChunkCoord(x),
                ChunkPosUtil.posToChunkCoord(z)
        );
    }

    public static ChunkPos of(Vec3 pos) {
        return ChunkPosUtil.of(
                ChunkPosUtil.posToChunkCoord(pos.x()),
                ChunkPosUtil.posToChunkCoord(pos.z())
        );
    }

    public static ChunkPos of(Entity entity) {
        return ChunkPosUtil.of(entity.position());
    }

    protected ChunkPosUtil() { }

}
