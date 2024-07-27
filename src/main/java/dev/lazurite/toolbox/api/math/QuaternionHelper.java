package dev.lazurite.toolbox.api.math;

import net.minecraft.nbt.NbtCompound;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A useful helper for dealing with Minecraft {@link Quaternionf} objects.
 * @since 1.0.0
 */
public class QuaternionHelper {
    /**
     * Rotate the given {@link Quaternionf} by the given number of degrees on the X axis.
     * @param quat the {@link Quaternionf} to perform the operation on
     * @param deg number of degrees to rotate by
     */
    public static Quaternionf rotateX(Quaternionf quat, double deg) {
        var radHalfAngle = Math.toRadians(deg) / 2.0;
        quat.mul(new Quaternionf((float) Math.sin(radHalfAngle), 0, 0, (float) Math.cos(radHalfAngle)));
        return quat;
    }

    /**
     * Rotate the given {@link Quaternionf} by the given number of degrees on the Y axis.
     * @param quat the {@link Quaternionf} to perform the operation on
     * @param deg number of degrees to rotate by
     */
    public static Quaternionf rotateY(Quaternionf quat, double deg) {
        var radHalfAngle = Math.toRadians(deg) / 2.0;
        quat.mul(new Quaternionf(0, (float) Math.sin(radHalfAngle), 0, (float) Math.cos(radHalfAngle)));
        return quat;
    }

    /**
     * Rotate the given {@link Quaternionf} by the given number of degrees on the Z axis.
     * @param quat the {@link Quaternionf} to perform the operation on
     * @param deg number of degrees to rotate by
     */
    public static Quaternionf rotateZ(Quaternionf quat, double deg) {
        var radHalfAngle = Math.toRadians(deg) / 2.0;
        quat.mul(new Quaternionf(0, 0, (float) Math.sin(radHalfAngle), (float) Math.cos(radHalfAngle)));
        return quat;
    }

    /**
     * Converts the given {@link Quaternionf} to a vector containing three axes of rotation in degrees.
     * The order is (roll, pitch, yaw).
     * @param quat the {@link Quaternionf} to extract the euler angles from
     * @return a new vector containing three rotations in degrees
     */
    public static Vector3f toEulerAngles(Quaternionf quat) {
        final var q = new Quaternionf(0, 0, 0, 1);
        q.set(quat.x(), quat.y(), quat.z(), quat.w());

        var i = 0.0f;
        var j = 0.0f;
        var k = 0.0f;

        // roll (x-axis rotation)
        final var sinr_cosp = 2 * (q.w() * q.x() + q.y() * q.z());
        final var cosr_cosp = 1 - 2 * (q.x() * q.x() + q.y() * q.y());
        i = (float) Math.atan2(sinr_cosp, cosr_cosp);

        // pitch (y-axis rotation)
        final var sinp = 2 * (q.w() * q.y() - q.z() * q.x());
        if (Math.abs(sinp) >= 1) j = (float) Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
        else j = (float) Math.asin(sinp);

        // yaw (z-axis rotation)
        final var siny_cosp = 2 * (q.w() * q.z() + q.x() * q.y());
        final var cosy_cosp = 1 - 2 * (q.y() * q.y() + q.z() * q.z());
        k = (float) Math.atan2(siny_cosp, cosy_cosp);

        return new Vector3f(i, j ,k);
    }

    /**
     * Stores the given {@link Quaternionf} into a new {@link NbtCompound}.
     * @param quat the {@link Quaternionf} to store
     * @return the new {@link NbtCompound}
     */
    public static NbtCompound toTag(Quaternionf quat) {
        final var tag = new NbtCompound();
        tag.putFloat("i", quat.x());
        tag.putFloat("j", quat.y());
        tag.putFloat("k", quat.z());
        tag.putFloat("r", quat.w());
        return tag;
    }

    /**
     * Retrieves a {@link Quaternionf} from the given {@link NbtCompound}.
     * @param tag the {@link NbtCompound} to retrieve the {@link Quaternionf} from
     * @return the new {@link Quaternionf}
     */
    public static Quaternionf fromTag(NbtCompound tag) {
        return new Quaternionf(tag.getFloat("i"), tag.getFloat("j"), tag.getFloat("k"), tag.getFloat("r"));
    }

    /**
     * Gets the yaw rotation from the given {@link Quaternionf}.
     * @param quat the {@link Quaternionf} to get the angle from
     * @return the yaw angle
     */
    public static float getYaw(Quaternionf quat) {
        return -1 * (float) Math.toDegrees(toEulerAngles(quat).z());
    }

    /**
     * Gets the pitch rotation from the given {@link Quaternionf}.
     * @param quat the {@link Quaternionf} to get the angle from
     * @return the pitch angle
     */
    public static float getPitch(Quaternionf quat) {
        return (float) Math.toDegrees(toEulerAngles(quat).y());
    }

    /**
     * Gets the roll rotation from the given {@link Quaternionf}.
     * @param quat the {@link Quaternionf} to get the angle from
     * @return the roll angle
     */
    public static float getRoll(Quaternionf quat) {
        return (float) Math.toDegrees(toEulerAngles(quat).x());
    }

    /**
     * Lerp, but for spherical stuff (hence Slerp).
     * @param q1 the first {@link Quaternionf} to slerp
     * @param q2 the second {@link Quaternionf} to slerp
     * @param t  the delta time
     * @return the slerped {@link Quaternionf}
     */
    public static Quaternionf slerp(Quaternionf q1, Quaternionf q2, float t) {
        q1.normalize();
        q2.normalize();

        if (q1.x() == q2.x() && q1.y() == q2.y() && q1.z() == q2.z() && q1.w() == q2.w()) {
            return new Quaternionf(q1.x(), q1.y(), q1.z(), q1.w());
        }

        var result = (q1.x() * q2.x()) + (q1.y() * q2.y()) + (q1.z() * q2.z()) + (q1.w() * q2.w());

        if (result < 0.0f) {
            q2.set(-q2.x(), -q2.y(), -q2.z(), -q2.w());
            result = -result;
        }

        var scale0 = 1 - t;
        var scale1 = t;

        if ((1 - result) > 0.1f) {
            final var theta = (float) Math.acos(result);
            final var invSinTheta = 1f / (float) Math.sin(theta);

            scale0 = (float) Math.sin((1 - t) * theta) * invSinTheta;
            scale1 = (float) Math.sin((t * theta)) * invSinTheta;
        }

        final var out = new Quaternionf(
                (scale0 * q1.x()) + (scale1 * q2.x()),
                (scale0 * q1.y()) + (scale1 * q2.y()),
                (scale0 * q1.z()) + (scale1 * q2.z()),
                (scale0 * q1.w()) + (scale1 * q2.w()));

        out.normalize();
        return out;
    }

    public static float dot(Quaternionf q1, Quaternionf q2) {
        return q1.x() * q2.x() +
                q1.y() * q2.y() +
                q1.z() * q2.z() +
                q1.w() * q2.w();
    }
}
