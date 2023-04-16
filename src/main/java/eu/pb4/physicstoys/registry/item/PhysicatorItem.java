package eu.pb4.physicstoys.registry.item;

import com.jme3.math.Vector3f;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class PhysicatorItem extends Item implements PolymerItem {
    public PhysicatorItem(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.DEBUG_STICK;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var blockState = context.getWorld().getBlockState(context.getBlockPos());
        context.getWorld().setBlockState(context.getBlockPos(), Blocks.AIR.getDefaultState(), Block.FORCE_STATE);

        var entity = BlockPhysicsEntity.create(context.getWorld(), blockState, context.getBlockPos());
        context.getWorld().spawnEntity(entity);

        return super.useOnBlock(context);
    }
}
