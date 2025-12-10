package eu.pb4.rayon.impl.mixin.common.entity;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.rayon.api.EntityPhysicsElement;
import eu.pb4.rayon.api.PhysicsElement;
import eu.pb4.rayon.impl.bullet.math.Convert;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows {@link PhysicsElement} objects to be affected by explosions.
 */
@Mixin(ServerExplosion.class)
public class ExplosionMixin {
    @ModifyArg(
            method = "hurtEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    public Vec3 setVelocity(Vec3 velocity, @Local Entity entity) {
        if (EntityPhysicsElement.is(entity)) {
            var element = EntityPhysicsElement.get(entity);
            element.getRigidBody().applyCentralImpulse(Convert.toBullet(velocity).multLocal(element.getRigidBody().getMass() * 100f));
        }

        return velocity;
    }
}