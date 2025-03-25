package eu.pb4.physicstoys.registry.item;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import eu.pb4.rayon.impl.bullet.math.Convert;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class PhysicsTntCannonItem extends Item implements VanillaModeledPolymerItem {
    public PhysicsTntCannonItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.LEATHER_HORSE_ARMOR;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user.isCreative() || user.getInventory().containsAny(x -> x.isOf(USRegistry.PHYSICAL_TNT_ITEM))) {
            if (!user.isCreative()) {
                user.getInventory().remove(x -> x.isOf(USRegistry.PHYSICAL_TNT_ITEM), 1, new SimpleInventory());
            }
            var entity = PhysicalTntEntity.of(user.getWorld(), user.getX(), user.getEyeY(), user.getZ(), user);
            entity.getRigidBody().applyCentralImpulse(Convert.toBullet(user.getRotationVec(0).multiply(350)));
            user.getWorld().spawnEntity(entity);
            user.getItemCooldownManager().set(user.getStackInHand(hand), 5);
            return ActionResult.SUCCESS_SERVER;
        }

        return ActionResult.FAIL;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
        out.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xFF2222));
    }
}
