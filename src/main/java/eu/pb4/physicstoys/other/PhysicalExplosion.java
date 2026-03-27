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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhysicalExplosion extends ServerExplosion {
    private final Level world;
    private final float power;
    private final double x;
    private final double y;
    private final double z;
    private final Player player;
    private final NameAndId playerProfile;

    public PhysicalExplosion(ServerLevel world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator behavior, Vec3 pos, float power, boolean createFire, Explosion.BlockInteraction destructionType) {
        super(world, entity, damageSource, behavior, pos, power, createFire, destructionType);
        this.world = world;
        this.power = power;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.player = entity instanceof TraceableEntity && ((TraceableEntity) entity).getOwner() instanceof Player player ? player : null;
        this.playerProfile = entity instanceof PhysicalTntEntity physicalTntEntity && physicalTntEntity.ownerProfile != null ? physicalTntEntity.ownerProfile : CommonProtection.UNKNOWN;
    }


    @Override
    protected void interactWithBlocks(List<BlockPos> positions) {
        Util.shuffle(positions, this.world.getRandom());
        for (var blockPos : positions) {
            var blockState = this.world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (!blockState.isAir() && !blockState.is(BlockTags.REPLACEABLE)) {
                var vec = Vec3.atCenterOf(blockPos).subtract(this.x, this.y, this.z);

                var l = vec.length();

                vec = vec.normalize().scale(Math.min(this.power * 120 / l, 400));

                if (vec.lengthSqr() > 1) {
                    this.world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    block.wasExploded((ServerLevel) this.world, blockPos, this);
                    if (!blockState.is(Blocks.TNT) && !blockState.is(USRegistry.PHYSICAL_TNT_BLOCK) && !blockState.is(BlockTags.REPLACEABLE)
                            && CommonProtection.canBreakBlock(this.world, blockPos, this.playerProfile, this.player)) {
                        var e = BlockPhysicsEntity.create(world, blockState, blockPos);
                        e.setDespawnTimer(20 * 5);
                        e.getRigidBody().applyCentralImpulse(Convert.toBullet(vec));
                        this.world.addFreshEntity(e);
                    }
                }
            }
        }
    }
}
