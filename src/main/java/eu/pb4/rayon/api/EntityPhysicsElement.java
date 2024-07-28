package eu.pb4.rayon.api;

import eu.pb4.rayon.impl.bullet.collision.body.EntityRigidBody;
import eu.pb4.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Use this interface to create a physics entity.
 * @see PhysicsElement
 */
public interface EntityPhysicsElement extends PhysicsElement<Entity> {
    static boolean is(Entity entity) {
        return entity instanceof EntityPhysicsElement element && element.getRigidBody() != null;
    }

    static EntityPhysicsElement get(Entity entity) {
        return (EntityPhysicsElement) entity;
    }

    @Override @Nullable
    EntityRigidBody getRigidBody();

    @Override
    default MinecraftShape.Convex createShape() {
        final var box = cast().getBoundingBox();
        return MinecraftShape.convex(box.shrink(box.getLengthX() * 0.25, box.getLengthY() * 0.25, box.getLengthZ() * 0.25));
    }

    default boolean skipVanillaEntityCollisions() {
        return false;
    }
}
