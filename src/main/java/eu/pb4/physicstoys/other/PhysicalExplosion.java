package eu.pb4.physicstoys.other;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class PhysicalExplosion extends Explosion {
    private final World world;
    private final float power;
    private final double x;
    private final double y;
    private final double z;
    private final PlayerEntity player;
    private final GameProfile playerProfile;

    public PhysicalExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
        super(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
        this.world = world;
        this.power = power;
        this.x = x;
        this.y = y;
        this.z = z;
        this.player = entity instanceof Ownable && ((Ownable) entity).getOwner() instanceof PlayerEntity player ? player : null;
        this.playerProfile = entity instanceof PhysicalTntEntity physicalTntEntity && physicalTntEntity.ownerProfile != null ? physicalTntEntity.ownerProfile : CommonProtection.UNKNOWN;
    }


    @Override
    public void affectWorld(boolean particles) {
        this.world.playSound(this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);

        boolean bl = this.shouldDestroy();
        ((ServerWorld) this.world).spawnParticles(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 0, 1.0D, 0.0D, 0.0D, 1);

        var affectedBlocks = (ObjectArrayList<BlockPos>) this.getAffectedBlocks();

        if (bl) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList();
            Util.shuffle(affectedBlocks, this.world.random);
            ObjectListIterator var5 = affectedBlocks.iterator();

            while (var5.hasNext()) {
                BlockPos blockPos = (BlockPos) var5.next();
                BlockState blockState = this.world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (!blockState.isAir() && !blockState.isIn(BlockTags.REPLACEABLE_PLANTS)) {
                    var vec = Vec3d.ofCenter(blockPos).subtract(this.x, this.y, this.z);

                    var l = vec.length();

                    vec = vec.normalize().multiply(Math.min(this.power * 120 / l, 400));

                    if (vec.lengthSquared() > 1) {
                        this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                        block.onDestroyedByExplosion(this.world, blockPos, this);
                        if (!blockState.isOf(Blocks.TNT) && !blockState.isOf(USRegistry.PHYSICAL_TNT_BLOCK) && !blockState.isIn(BlockTags.REPLACEABLE_PLANTS)
                                && CommonProtection.canBreakBlock(this.world, blockPos, this.playerProfile, this.player)) {
                            var e = BlockPhysicsEntity.create(world, blockState, blockPos);
                            e.setDespawnTimer(20 * 5);
                            e.getRigidBody().applyCentralImpulse(Convert.toBullet(vec));
                            this.world.spawnEntity(e);
                        }
                    }


                    this.world.getProfiler().pop();
                }
            }

            var5 = objectArrayList.iterator();

            while (var5.hasNext()) {
                Pair<ItemStack, BlockPos> pair = (Pair) var5.next();
                Block.dropStack(this.world, pair.getSecond(), pair.getFirst());
            }

            this.clearAffectedBlocks();
        }
    }
}
