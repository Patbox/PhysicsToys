package eu.pb4.physicstoys.registry.item;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import eu.pb4.rayon.impl.bullet.math.Convert;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import eu.pb4.physicstoys.registry.entity.BasePhysicsEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class BaseballBatItem extends Item implements VanillaModeledPolymerItem, PhysicsEntityInteractor {
    public BaseballBatItem(Properties settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.STICK;
    }

    @Override
    public void onInteractWith(Player player, ItemStack stack, Vec3 hitPos, BasePhysicsEntity basePhysics) {
    }

    @Override
    public void onAttackWith(ServerPlayer player, ItemStack stack, BasePhysicsEntity basePhysics) {
        basePhysics.getRigidBody().applyCentralImpulse(Convert.toBullet(player.getViewVector(0).scale(250 * Math.log(basePhysics.getRigidBody().getMass() * 2))));
        basePhysics.setOwner(player.getGameProfile());
    }
}
