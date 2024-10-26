package eu.pb4.physicstoys.registry.item;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import eu.pb4.rayon.impl.bullet.math.Convert;
import eu.pb4.physicstoys.registry.entity.BasePhysicsEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class BaseballBatItem extends Item implements VanillaModeledPolymerItem, PhysicsEntityInteractor {
    public BaseballBatItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.STICK;
    }

    @Override
    public void onInteractWith(PlayerEntity player, ItemStack stack, Vec3d hitPos, BasePhysicsEntity basePhysics) {
    }

    @Override
    public void onAttackWith(ServerPlayerEntity player, ItemStack stack, BasePhysicsEntity basePhysics) {
        basePhysics.getRigidBody().applyCentralImpulse(Convert.toBullet(player.getRotationVec(0).multiply(250 * Math.log(basePhysics.getRigidBody().getMass() * 2))));
        basePhysics.setOwner(player.getGameProfile());
    }
}
