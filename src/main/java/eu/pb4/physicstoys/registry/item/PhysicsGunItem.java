package eu.pb4.physicstoys.registry.item;

import com.jme3.math.Vector3f;
import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import eu.pb4.rayon.impl.bullet.math.Convert;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.BasePhysicsEntity;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class PhysicsGunItem extends Item implements VanillaModeledPolymerItem, PhysicsEntityInteractor {
    public PhysicsGunItem(Properties settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.LEATHER_HORSE_ARMOR;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (stack.has(USRegistry.TARGET_COMPONENT) && entity instanceof ServerPlayer player) {
            var target = ((ServerLevel) world).getEntity(stack.get(USRegistry.TARGET_COMPONENT));
            if (target instanceof BasePhysicsEntity basePhysics) {
                if (slot != null) {
                    basePhysics.setHolder((Player) entity);
                    HitResult cast;// = entity.raycast(3, 0, false);
                    {
                        var maxDistance = 3;
                        Vec3 vec3d = entity.getEyePosition(0);
                        Vec3 vec3d2 = entity.getViewVector(0);
                        Vec3 vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                        cast = world.clip(new ClipContext(vec3d, vec3d3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, basePhysics));
                    }

                    var previous = basePhysics.getRigidBody().getPhysicsLocation(new Vector3f());

                    basePhysics.getRigidBody().setPhysicsLocation(previous.mult(0.6f).add(Convert.toBullet(cast.getLocation()).mult(0.4f)));
                    basePhysics.getRigidBody().setLinearVelocity(basePhysics.getRigidBody().getFrame().getLocationDelta(new Vector3f()).mult(10));
                } else {
                    stack.remove(USRegistry.TARGET_COMPONENT);
                    basePhysics.getRigidBody().activate();
                    basePhysics.setHolder(null);
                }
            } else {
                stack.remove(USRegistry.TARGET_COMPONENT);
            }
        }
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        var stack = user.getItemInHand(hand);
        if (stack.has(USRegistry.TARGET_COMPONENT)) {
            var pickTime = stack.getOrDefault(USRegistry.PICK_TIME_COMPONENT, 0L);
            if (world.getGameTime() - pickTime < 5) {
                return InteractionResult.FAIL;
            }

            var target = ((ServerLevel) world).getEntity(stack.get(USRegistry.TARGET_COMPONENT));
            if (target instanceof BasePhysicsEntity basePhysics) {
                basePhysics.getRigidBody().activate();
                basePhysics.setHolder(null);
                stack.remove(USRegistry.TARGET_COMPONENT);
                return InteractionResult.SUCCESS_SERVER;
            }
            stack.remove(USRegistry.TARGET_COMPONENT);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getItemInHand().has(USRegistry.TARGET_COMPONENT)) {
            return InteractionResult.FAIL;
        }

        var blockState = context.getLevel().getBlockState(context.getClickedPos());
        context.getLevel().setBlockAndUpdate(context.getClickedPos(), Blocks.AIR.defaultBlockState());
        context.getItemInHand().set(USRegistry.PICK_TIME_COMPONENT, context.getLevel().getGameTime());

        if (blockState.is(USRegistry.PHYSICAL_TNT_BLOCK)) {
            var vec = Vec3.atCenterOf(context.getClickedPos());
            var entity = PhysicalTntEntity.of(context.getLevel(), vec.x, vec.y, vec.z, context.getPlayer());
            entity.setHolder(context.getPlayer());
            context.getItemInHand().set(USRegistry.TARGET_COMPONENT, entity.getUUID());
            context.getLevel().addFreshEntity(entity);
        } else {
            var entity = BlockPhysicsEntity.create(context.getLevel(), blockState, context.getClickedPos());
            entity.setDespawnTimer(5 * 20);
            entity.setHolder(context.getPlayer());
            context.getItemInHand().set(USRegistry.TARGET_COMPONENT, entity.getUUID());
            context.getLevel().addFreshEntity(entity);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        out.set(DataComponents.DYED_COLOR, new DyedItemColor(stack.has(USRegistry.TARGET_COMPONENT) ? 0xffe357 : 0xbd7100));
    }

    @Override
    public void onInteractWith(Player player, ItemStack stack, Vec3 hitPos, BasePhysicsEntity basePhysics) {
        basePhysics.setOwner(player.getGameProfile());
        if (stack.has(USRegistry.TARGET_COMPONENT) && basePhysics.getHolder() == player) {
            basePhysics.setHolder(null);
            stack.remove(USRegistry.TARGET_COMPONENT);
        } else {
            if (stack.has(USRegistry.TARGET_COMPONENT)) {
                return;
            }

            stack.set(USRegistry.TARGET_COMPONENT, basePhysics.getUUID());
            basePhysics.setHolder(player);
            stack.set(USRegistry.PICK_TIME_COMPONENT, player.level().getGameTime());
        }
    }

    @Override
    public void onAttackWith(ServerPlayer player, ItemStack stack, BasePhysicsEntity basePhysics) {
        if (stack.has(USRegistry.TARGET_COMPONENT) && basePhysics.getHolder() == player) {
            basePhysics.getRigidBody().applyCentralImpulse(Convert.toBullet(player.getViewVector(0).scale(200)));
            basePhysics.setHolder(null);
            basePhysics.setOwner(player.getGameProfile());
            if (basePhysics instanceof BlockPhysicsEntity blockPhysicsEntity && !(basePhysics instanceof PhysicalTntEntity)) {
                blockPhysicsEntity.setDespawnTimer(10 * 20);
            }
            stack.remove(USRegistry.TARGET_COMPONENT);
        }
    }
}
