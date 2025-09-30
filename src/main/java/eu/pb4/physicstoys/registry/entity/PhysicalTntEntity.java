package eu.pb4.physicstoys.registry.entity;

import eu.pb4.physicstoys.other.PhysicalExplosion;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.rayon.impl.bullet.collision.body.ElementRigidBody;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PhysicalTntEntity extends BlockPhysicsEntity implements Ownable {
    private static final int DEFAULT_FUSE = 80;

    @Nullable
    private LivingEntity causingEntity;
    private int fuse;

    public PhysicalTntEntity(EntityType<PhysicalTntEntity> entityType, World world) {
        super((EntityType<BlockPhysicsEntity>) (Object) entityType, world);
        this.intersectionChecked = true;
        this.setBlockState(Blocks.TNT.getDefaultState());
        this.getRigidBody().setMass(8);
        this.getRigidBody().setBuoyancyType(ElementRigidBody.BuoyancyType.NONE);
        //this.getRigidBody().setProtectGravity(true);
        //this.getRigidBody().setGravity(new Vector3f(0, 10f, 0));
    }

    public static PhysicalTntEntity of(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        var self = new PhysicalTntEntity(USRegistry.TNT_ENTITY, world);
        self.setPosition(x, y, z);
        self.setFuse(DEFAULT_FUSE);
        self.causingEntity = igniter;
        return self;
    }

    @Override
    protected Text getDefaultName() {
        return super.getType().getName();
    }

    @Override
    protected void recalculateProperties() {

    }

    protected MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    public boolean canHit() {
        return !this.isRemoved();
    }

    public void tick() {
        int i = this.getFuse() - 1;
        this.setFuse(i);
        ((BlockDisplayElement) this.mainDisplayElement).setBlockState(this.fuse / 5 % 2 == 0 ? Blocks.WHITE_CONCRETE.getDefaultState() : Blocks.TNT.getDefaultState());
        if (i <= 0) {
            this.discard();
            if (!this.getEntityWorld().isClient()) {
                this.explode();
            }
        } else {
            this.updateWaterState();
            ((ServerWorld) this.getEntityWorld()).spawnParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.25D, this.getZ(), 0, 0.0D, 0.0D, 0.0D, 0);
        }

    }

    private void explode() {
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            float f = 5F;
            var pos = this.getBoundingBox().getCenter();

            var explosion = new PhysicalExplosion(serverWorld, this, null, null, pos, f, false, Explosion.DestructionType.DESTROY_WITH_DECAY);
            var c = explosion.explode();

            ParticleEffect particleEffect = explosion.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;
            for (var player : serverWorld.getPlayers())
                if (player.squaredDistanceTo(pos) < 4096.0) {
                    var optional = Optional.ofNullable(explosion.getKnockbackByPlayer().get(player));
                    player.networkHandler.sendPacket(new ExplosionS2CPacket(pos, f, c, optional, particleEffect, SoundEvents.ENTITY_GENERIC_EXPLODE,
                            Pool.<BlockParticleEffect>builder().add(new BlockParticleEffect(ParticleTypes.POOF, 0.5F, 1.0F)).add(new BlockParticleEffect(ParticleTypes.SMOKE, 1.0F, 1.0F)).build()));
                }
        }

    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putShort("Fuse", (short) this.getFuse());
        super.writeCustomData(view);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setFuse(view.getShort("Fuse", (short) 0));
        super.readCustomData(view);
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.causingEntity;
    }

    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0.15F;
    }

    public int getFuse() {
        return this.fuse;
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
    }

}
