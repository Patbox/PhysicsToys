package eu.pb4.physicstoys.mixin;

import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.event.network.EntityNetworking;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityNetworking.class)
public interface EntityNetworkingMixin {
    @Inject(method = {"sendMovement", "sendProperties"}, at = @At("HEAD"), cancellable = true, remap = false)
    private static void physicstoys$cancelPackets(EntityRigidBody rigidBody, CallbackInfo ci) {
        if (rigidBody.getElement().cast() instanceof PolymerEntity) {
            ci.cancel();
        }
    }
}
