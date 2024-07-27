package eu.pb4.physicstoys.registry.entity;

import com.jme3.math.Vector3f;
import com.simsilica.mathd.Vec3d;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import eu.pb4.physicstoys.other.PhysicalExplosion;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

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
        self.prevX = x;
        self.prevY = y;
        self.prevZ = z;
        self.causingEntity = igniter;
        return self;
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
            if (!this.getWorld().isClient) {
                this.explode();
            }
        } else {
            this.updateWaterState();
            ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.25D, this.getZ(), 0, 0.0D, 0.0D, 0.0D, 0);
        }

    }

    private void explode() {
        float f = 5F;

        Explosion explosion = new PhysicalExplosion(this.getWorld(), this, null, null, this.getX(), this.getY(), this.getZ(), f, false, Explosion.DestructionType.DESTROY_WITH_DECAY);
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(true);

    }

    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("Fuse", (short)this.getFuse());
        super.writeCustomDataToNbt(nbt);
    }

    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setFuse(nbt.getShort("Fuse"));
        super.readCustomDataFromNbt(nbt);
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.causingEntity;
    }

    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0.15F;
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
    }

    public int getFuse() {
        return this.fuse;
    }
}
