package eu.pb4.physicstoys.other;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import eu.pb4.rayon.impl.bullet.math.Convert;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhysicalExplosion extends ExplosionImpl {
    private final World world;
    private final float power;
    private final double x;
    private final double y;
    private final double z;
    private final PlayerEntity player;
    private final GameProfile playerProfile;

    public PhysicalExplosion(ServerWorld world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, Explosion.DestructionType destructionType) {
        super(world, entity, damageSource, behavior, pos, power, createFire, destructionType);
        this.world = world;
        this.power = power;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.player = entity instanceof Ownable && ((Ownable) entity).getOwner() instanceof PlayerEntity player ? player : null;
        this.playerProfile = entity instanceof PhysicalTntEntity physicalTntEntity && physicalTntEntity.ownerProfile != null ? physicalTntEntity.ownerProfile : CommonProtection.UNKNOWN;
    }


    @Override
    protected void destroyBlocks(List<BlockPos> positions) {
        Util.shuffle(positions, this.world.random);
        for (var blockPos : positions) {
            var blockState = this.world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (!blockState.isAir() && !blockState.isIn(BlockTags.REPLACEABLE)) {
                var vec = Vec3d.ofCenter(blockPos).subtract(this.x, this.y, this.z);

                var l = vec.length();

                vec = vec.normalize().multiply(Math.min(this.power * 120 / l, 400));

                if (vec.lengthSquared() > 1) {
                    this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                    block.onDestroyedByExplosion((ServerWorld) this.world, blockPos, this);
                    if (!blockState.isOf(Blocks.TNT) && !blockState.isOf(USRegistry.PHYSICAL_TNT_BLOCK) && !blockState.isIn(BlockTags.REPLACEABLE)
                            && CommonProtection.canBreakBlock(this.world, blockPos, this.playerProfile, this.player)) {
                        var e = BlockPhysicsEntity.create(world, blockState, blockPos);
                        e.setDespawnTimer(20 * 5);
                        e.getRigidBody().applyCentralImpulse(Convert.toBullet(vec));
                        this.world.spawnEntity(e);
                    }
                }
            }
        }
    }
}
