package eu.pb4.rayon.impl.mixin.common.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import eu.pb4.rayon.api.EntityPhysicsElement;
import eu.pb4.rayon.impl.bullet.math.Convert;
import eu.pb4.rayon.api.math.QuaternionHelper;
import eu.pb4.rayon.api.math.VectorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Basic changes for {@link EntityPhysicsElement}s. ({@link CallbackInfo#cancel()} go brrr)
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow protected abstract void tickInVoid();

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

    @Inject(method = "writeData", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomData(Lnet/minecraft/storage/WriteView;)V"))
    public void saveWithoutId(WriteView view, CallbackInfo ci) {
        if (EntityPhysicsElement.is((Entity) (Object) this)) {
            var rigidBody = EntityPhysicsElement.get((Entity) (Object) this).getRigidBody();
            view.put("orientation", Codecs.QUATERNION_F, Convert.toMinecraft(rigidBody.getPhysicsRotation(new Quaternion())));
            view.put("linearVelocity", Codecs.VECTOR_3F, Convert.toMinecraft(rigidBody.getLinearVelocity(new Vector3f())));
            view.put("angularVelocity", Codecs.VECTOR_3F, Convert.toMinecraft(rigidBody.getAngularVelocity(new Vector3f())));
            view.putFloat("mass", rigidBody.getMass());
            view.putFloat("dragCoefficient", rigidBody.getDragCoefficient());
            view.putFloat("friction", rigidBody.getFriction());
            view.putFloat("restitution", rigidBody.getRestitution());
            view.putBoolean("terrainLoadingEnabled", rigidBody.terrainLoadingEnabled());
            view.putInt("buoyancyType", rigidBody.getBuoyancyType().ordinal());
            view.putInt("dragType", rigidBody.getDragType().ordinal());
        }
    }

    @Inject(method = "readData", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomData(Lnet/minecraft/storage/ReadView;)V"))
    public void load(ReadView view, CallbackInfo ci) {
        if (EntityPhysicsElement.is((Entity) (Object) this)) {
            EntityPhysicsElement.get((Entity) (Object) this).getRigidBody().readTagInfo(view);
        }
    }
}
