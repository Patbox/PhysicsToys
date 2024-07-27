package dev.lazurite.rayon.impl.mixin.common.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.api.PhysicsElement;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows {@link PhysicsElement} objects to be affected by explosions.
 */
@Mixin(Explosion.class)
public class ExplosionMixin {
    @Unique
    private Entity entity;

    @Inject(
            method = "collectBlocksAndDamageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion(Lnet/minecraft/world/explosion/Explosion;)Z"
            )
    )
    public void collectBlocksAndDamageEntities(CallbackInfo ci, @Local Entity entity) {
        this.entity = entity;
    }

    @ModifyArg(
            method = "collectBlocksAndDamageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
            )
    )
    public Vec3d setVelocity(Vec3d velocity) {
        if (EntityPhysicsElement.is(entity)) {
            var element = EntityPhysicsElement.get(entity);
            element.getRigidBody().applyCentralImpulse(Convert.toBullet(velocity).multLocal(element.getRigidBody().getMass() * 100f));
        }

        return velocity;
    }
}