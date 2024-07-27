package dev.lazurite.rayon.impl.mixin.common.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.toolbox.api.math.QuaternionHelper;
import dev.lazurite.toolbox.api.math.VectorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Basic changes for {@link EntityPhysicsElement}s. ({@link CallbackInfo#cancel()} go brrr)
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void pushAwayFrom(Entity entity, CallbackInfo info) {
        if (EntityPhysicsElement.is((Entity) (Object) this) && EntityPhysicsElement.is(entity)) {
            info.cancel();
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(CallbackInfo info) {
        if (EntityPhysicsElement.is((Entity) (Object) this)) {
            info.cancel();
        }
    }

    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    public void saveWithoutId(NbtCompound tag, CallbackInfoReturnable<NbtCompound> info) {
        if (EntityPhysicsElement.is((Entity) (Object) this)) {
            var rigidBody = EntityPhysicsElement.get((Entity) (Object) this).getRigidBody();
            tag.put("orientation", QuaternionHelper.toTag(Convert.toMinecraft(rigidBody.getPhysicsRotation(new Quaternion()))));
            tag.put("linearVelocity", VectorHelper.toTag(Convert.toMinecraft(rigidBody.getLinearVelocity(new Vector3f()))));
            tag.put("angularVelocity", VectorHelper.toTag(Convert.toMinecraft(rigidBody.getAngularVelocity(new Vector3f()))));
            tag.putFloat("mass", rigidBody.getMass());
            tag.putFloat("dragCoefficient", rigidBody.getDragCoefficient());
            tag.putFloat("friction", rigidBody.getFriction());
            tag.putFloat("restitution", rigidBody.getRestitution());
            tag.putBoolean("terrainLoadingEnabled", rigidBody.terrainLoadingEnabled());
            tag.putInt("buoyancyType", rigidBody.getBuoyancyType().ordinal());
            tag.putInt("dragType", rigidBody.getDragType().ordinal());
        }
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    public void load(NbtCompound tag, CallbackInfo info) {
        if (EntityPhysicsElement.is((Entity) (Object) this)) {
            EntityPhysicsElement.get((Entity) (Object) this).getRigidBody().readTagInfo(tag);
        }
    }
}
