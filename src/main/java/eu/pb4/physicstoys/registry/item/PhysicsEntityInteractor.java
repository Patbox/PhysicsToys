package eu.pb4.physicstoys.registry.item;

import eu.pb4.physicstoys.registry.entity.BasePhysicsEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public interface PhysicsEntityInteractor {
    void onInteractWith(PlayerEntity player, ItemStack stack, Vec3d hitPos, BasePhysicsEntity basePhysics);

    void onAttackWith(ServerPlayerEntity player, ItemStack stack, BasePhysicsEntity basePhysics);
}
