package eu.pb4.physicstoys.registry.item;

import eu.pb4.physicstoys.registry.entity.BasePhysicsEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface PhysicsEntityInteractor {
    void onInteractWith(Player player, ItemStack stack, Vec3 hitPos, BasePhysicsEntity basePhysics);

    void onAttackWith(ServerPlayer player, ItemStack stack, BasePhysicsEntity basePhysics);
}
