package eu.pb4.physicstoys.registry.item;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.BasePhysicsEntity;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PhysicsTntCannonItem extends Item implements PolymerItem {

    public PhysicsTntCannonItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.LEATHER_HORSE_ARMOR;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isCreative() || user.getInventory().containsAny(x -> x.isOf(USRegistry.PHYSICAL_TNT_ITEM))) {
            if (!user.isCreative()) {
                user.getInventory().remove(x -> x.isOf(USRegistry.PHYSICAL_TNT_ITEM), 1, new SimpleInventory());
            }
            var entity = PhysicalTntEntity.of(user.world, user.getX(), user.getEyeY(), user.getZ(), user);
            entity.getRigidBody().applyCentralImpulse(Convert.toBullet(user.getRotationVec(0).multiply(350)));
            user.getWorld().spawnEntity(entity);
            user.getItemCooldownManager().set(this, 5);
            return TypedActionResult.success(user.getStackInHand(hand), true);
        }

        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return 0xFF2222;
    }
}
