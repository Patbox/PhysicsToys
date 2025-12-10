package eu.pb4.physicstoys.registry.entity;

import eu.pb4.physicstoys.other.PhysicalExplosion;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.rayon.impl.bullet.collision.body.ElementRigidBody;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PhysicalTntEntity extends BlockPhysicsEntity implements TraceableEntity {
    private static final int DEFAULT_FUSE = 80;

    @Nullable
    private LivingEntity causingEntity;
    private int fuse;

    public PhysicalTntEntity(EntityType<PhysicalTntEntity> entityType, Level world) {
        super((EntityType<BlockPhysicsEntity>) (Object) entityType, world);
        this.blocksBuilding = true;
        this.setBlockState(Blocks.TNT.defaultBlockState());
        this.getRigidBody().setMass(8);
        this.getRigidBody().setBuoyancyType(ElementRigidBody.BuoyancyType.NONE);
        //this.getRigidBody().setProtectGravity(true);
        //this.getRigidBody().setGravity(new Vector3f(0, 10f, 0));
    }

    public static PhysicalTntEntity of(Level world, double x, double y, double z, @Nullable LivingEntity igniter) {
        var self = new PhysicalTntEntity(USRegistry.TNT_ENTITY, world);
        self.setPos(x, y, z);
        self.setFuse(DEFAULT_FUSE);
        self.causingEntity = igniter;
        return self;
    }

    @Override
    protected Component getTypeName() {
        return super.getType().getDescription();
    }

    @Override
    protected void recalculateProperties() {

    }

    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    public boolean isPickable() {
        return !this.isRemoved();
    }

    public void tick() {
        int i = this.getFuse() - 1;
        this.setFuse(i);
        ((BlockDisplayElement) this.mainDisplayElement).setBlockState(this.fuse / 5 % 2 == 0 ? Blocks.WHITE_CONCRETE.defaultBlockState() : Blocks.TNT.defaultBlockState());
        if (i <= 0) {
            this.discard();
            if (!this.level().isClientSide()) {
                this.explode();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.25D, this.getZ(), 0, 0.0D, 0.0D, 0.0D, 0);
        }

    }

    private void explode() {
        if (this.level() instanceof ServerLevel serverWorld) {
            float f = 5F;
            var pos = this.getBoundingBox().getCenter();

            var explosion = new PhysicalExplosion(serverWorld, this, null, null, pos, f, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
            var c = explosion.explode();

            ParticleOptions particleEffect = explosion.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;
            for (var player : serverWorld.players())
                if (player.distanceToSqr(pos) < 4096.0) {
                    var optional = Optional.ofNullable(explosion.getHitPlayers().get(player));
                    player.connection.send(new ClientboundExplodePacket(pos, f, c, optional, particleEffect, SoundEvents.GENERIC_EXPLODE,
                            WeightedList.<ExplosionParticleInfo>builder().add(new ExplosionParticleInfo(ParticleTypes.POOF, 0.5F, 1.0F)).add(new ExplosionParticleInfo(ParticleTypes.SMOKE, 1.0F, 1.0F)).build()));
                }
        }

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        view.putShort("Fuse", (short) this.getFuse());
        super.addAdditionalSaveData(view);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        this.setFuse(view.getShortOr("Fuse", (short) 0));
        super.readAdditionalSaveData(view);
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.causingEntity;
    }

    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 0.15F;
    }

    public int getFuse() {
        return this.fuse;
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
    }

}
