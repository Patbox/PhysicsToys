package dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;

import java.util.List;

public interface EntitySupplier {

    default List<Entity> getInsideOf(ElementRigidBody rigidBody, Box box) {
        if (!rigidBody.isInWorld()) {
            return List.of();
        }

        return rigidBody.getSpace().getLevel().getEntitiesByClass(Entity.class, box,
                entity ->
                        // Entity can be a Boat, Minecart, or any LivingEntity so long as it is not a player in spectator mode.
                        (
                            entity instanceof BoatEntity ||
                            entity instanceof MinecartEntity ||
                            (
                                entity instanceof LivingEntity &&
                                !(entity instanceof PlayerEntity player && this.getGameType(player) == GameMode.SPECTATOR)
                            )
                        )
                        && !EntityPhysicsElement.is(entity));
    }

    GameMode getGameType(PlayerEntity player);
}
