package eu.pb4.physicstoys.registry.item;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import eu.pb4.rayon.impl.bullet.math.Convert;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import org.jetbrains.annotations.Nullable;

public class PhysicsTntCannonItem extends Item implements VanillaModeledPolymerItem {
    public PhysicsTntCannonItem(Properties settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.LEATHER_HORSE_ARMOR;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user.isCreative() || user.getInventory().hasAnyMatching(x -> x.is(USRegistry.PHYSICAL_TNT_ITEM))) {
            if (!user.isCreative()) {
                user.getInventory().clearOrCountMatchingItems(x -> x.is(USRegistry.PHYSICAL_TNT_ITEM), 1, new SimpleContainer());
            }
            var entity = PhysicalTntEntity.of(user.level(), user.getX(), user.getEyeY(), user.getZ(), user);
            entity.getRigidBody().applyCentralImpulse(Convert.toBullet(user.getViewVector(0).scale(350)));
            user.level().addFreshEntity(entity);
            user.getCooldowns().addCooldown(user.getItemInHand(hand), 5);
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        out.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF2222));
    }
}
