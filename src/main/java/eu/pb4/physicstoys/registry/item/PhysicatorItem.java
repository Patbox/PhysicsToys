package eu.pb4.physicstoys.registry.item;

import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class PhysicatorItem extends Item implements VanillaModeledPolymerItem {
    public PhysicatorItem(Properties settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.DEBUG_STICK;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var blockState = context.getLevel().getBlockState(context.getClickedPos());
        context.getLevel().setBlock(context.getClickedPos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);

        var entity = BlockPhysicsEntity.create(context.getLevel(), blockState, context.getClickedPos());
        context.getLevel().addFreshEntity(entity);

        return super.useOn(context);
    }
}
