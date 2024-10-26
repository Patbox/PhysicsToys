package eu.pb4.rayon.impl.mixin.common.entity;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.rayon.api.EntityPhysicsElement;
import eu.pb4.rayon.api.PhysicsElement;
import eu.pb4.rayon.impl.bullet.math.Convert;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows {@link PhysicsElement} objects to be affected by explosions.
 */
@Mixin(ExplosionImpl.class)
public class ExplosionMixin {
    @ModifyArg(
            method = "damageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
            )
    )
    public Vec3d setVelocity(Vec3d velocity, @Local Entity entity) {
        if (EntityPhysicsElement.is(entity)) {
            var element = EntityPhysicsElement.get(entity);
            element.getRigidBody().applyCentralImpulse(Convert.toBullet(velocity).multLocal(element.getRigidBody().getMass() * 100f));
        }

        return velocity;
    }
}