package dev.lazurite.toolbox.api.math;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * A useful helper for dealing with Minecraft vectors.
 * @since 1.0.0
 */
public class VectorHelper {
    public static Vector3f toVector3f(Vec3d Vec3) {
        return new Vector3f((float) Vec3.x, (float) Vec3.y, (float) Vec3.z);
    }

    public static Vec3d toVec3(Vector3f vector3f) {
        return new Vec3d(vector3f.x(), vector3f.y(), vector3f.z());
    }

    /**
     * Lerps two {@link Vector3f} objects using tick delta.
     * @param vec1 the first float vector
     * @param vec2 the second float vector
     * @param delta minecraft tick delta
     * @return the newly lerped {@link Vector3f}
     */
    public static Vector3f lerp(Vector3f vec1, Vector3f vec2, float delta) {
        return new Vector3f(
            MathHelper.lerp(delta, vec1.x(), vec2.x()),
            MathHelper.lerp(delta, vec1.y(), vec2.y()),
            MathHelper.lerp(delta, vec1.z(), vec2.z())
        );
    }

    /**
     * Lerps two {@link Vec3d} objects using tick delta.
     * @param vec1 the first double vector
     * @param vec2 the second double vector
     * @param delta minecraft tick delta
     * @return the newly lerped {@link Vec3d}
     */
    public static Vec3d lerp(Vec3d vec1, Vec3d vec2, float delta) {
        return new Vec3d(
                MathHelper.lerp(delta, vec1.x, vec2.x),
                MathHelper.lerp(delta, vec1.y, vec2.y),
                MathHelper.lerp(delta, vec1.z, vec2.z)
        );
    }

    /**
     * Converts the given {@link Vector3f} into a new {@link NbtCompound}.
     * @param vec the {@link Vector3f} to convert
     * @return the new {@link NbtCompound}
     */
    public static NbtCompound toTag(Vector3f vec) {
        final var tag = new NbtCompound();
        tag.putFloat("x", vec.x());
        tag.putFloat("y", vec.y());
        tag.putFloat("z", vec.z());
        return tag;
    }

    /**
     * Retrieves a {@link Vector3f} from the given {@link NbtCompound}.
     * @param tag the {@link NbtCompound} to retrieve the {@link Vector3f} from
     * @return the new {@link Vector3f}
     */
    public static Vector3f fromTag(NbtCompound tag) {
        return new Vector3f(
                tag.getFloat("x"),
                tag.getFloat("y"),
                tag.getFloat("z")
        );
    }
}
