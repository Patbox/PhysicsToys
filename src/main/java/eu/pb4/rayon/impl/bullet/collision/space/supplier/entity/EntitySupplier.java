package eu.pb4.rayon.impl.bullet.collision.space.supplier.entity;

import eu.pb4.rayon.api.EntityPhysicsElement;
import eu.pb4.rayon.impl.bullet.collision.body.ElementRigidBody;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

public interface EntitySupplier {

    default List<Entity> getInsideOf(ElementRigidBody rigidBody, AABB box) {
        if (!rigidBody.isInWorld()) {
            return List.of();
        }

        return rigidBody.getSpace().getLevel().getEntitiesOfClass(Entity.class, box,
                entity ->
                        // Entity can be a Boat, Minecart, or any LivingEntity so long as it is not a player in spectator mode.
                        (
                            entity instanceof Boat ||
                            entity instanceof Minecart ||
                            (
                                entity instanceof LivingEntity &&
                                !(entity instanceof Player player && this.getGameType(player) == GameType.SPECTATOR)
                            )
                        )
                        && !EntityPhysicsElement.is(entity));
    }

    GameType getGameType(Player player);
}
