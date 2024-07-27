package dev.lazurite.rayon.impl.bullet.collision.space.generator;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.math.Convert;

/**
 * Mods should implement it on their own, with will allow for better performance
 */
public class EntityCollisionGenerator {
    public static void step(MinecraftSpace space) {
        for (var rigidBody : space.getRigidBodiesByClass(EntityRigidBody.class)) {
            if (rigidBody.getElement().skipVanillaEntityCollisions()) {
                continue;
            }

            final var box = rigidBody.getCurrentBoundingBox();
            final var location = rigidBody.getPhysicsLocation(new Vector3f()).subtract(new Vector3f(0, -box.getYExtent(), 0));
            final var mass = rigidBody.getMass();

            final var vanillaBox = rigidBody.getCurrentMinecraftBoundingBox();

            for (var entity : space.getWorkerThread().getEntitySupplier().getInsideOf(rigidBody, vanillaBox)) {
                final var entityPos = Convert.toBullet(entity.getPos().add(0, entity.getBoundingBox().getLengthY(), 0));
                final var normal = location.subtract(entityPos).multLocal(new Vector3f(1, 0, 1)).normalize();

                final var intersection = entity.getBoundingBox().intersection(vanillaBox);
                final var force = normal.clone()
                        .multLocal((float) intersection.getAverageSideLength() / (float) vanillaBox.getAverageSideLength())
                        .multLocal(mass)
                        .multLocal(new Vector3f(1, 0, 1));
                rigidBody.applyCentralImpulse(force);
            }
        }
    }
}