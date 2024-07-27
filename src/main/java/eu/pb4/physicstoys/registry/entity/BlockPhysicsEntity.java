package eu.pb4.physicstoys.registry.entity;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.physicstoys.other.ShapeUtil;
import eu.pb4.physicstoys.registry.PhysicsTags;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class BlockPhysicsEntity extends BasePhysicsEntity {
    private BlockState currentBlockState = Blocks.AIR.getDefaultState();
    private int despawnTimerValue = -1;
    private int despawnTimer;

    public BlockPhysicsEntity(EntityType<BlockPhysicsEntity> type, World world) {
        super(type, world);
    }

    public static BlockPhysicsEntity create(World world, BlockState state, BlockPos pos) {
        var entity = new BlockPhysicsEntity(USRegistry.BLOCK_ENTITY, world);
        var vec = Vec3d.ofCenter(pos);
        entity.setBlockState(state);
        entity.setPosition(vec.x, vec.y, vec.z);
        entity.getRigidBody().setLinearVelocity(new Vector3f());
        entity.getRigidBody().setAngularVelocity(new Vector3f());
        entity.getRigidBody().setPhysicsLocation(Convert.toBullet(entity.getPos()));
        return entity;
    }

    @Override
    protected DisplayElement createMainDisplayElement() {
        var x = new BlockDisplayElement();
        x.setInterpolationDuration(1);
        return x;
    }

    @Override
    protected float getInteractionWidth() {
        return 1.1f;
    }

    @Override
    protected float getInteractionHeight() {
        return 1.1f;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    public void setBlockState(BlockState state) {
        if (state == this.currentBlockState) {
            return;
        }
        ((BlockDisplayElement) this.mainDisplayElement).setBlockState(state);
        this.currentBlockState = state;
        this.recalculateProperties();
        this.updateBody();
    }

    protected void recalculateProperties() {
        var x = (float) Math.min(Math.max(12f * Math.log1p(Math.max(this.currentBlockState.getBlock().getHardness(), this.currentBlockState.getBlock().getBlastResistance())), 5), 50);

        if (x < 0 || Float.isNaN(x)) {
            x = 60;
        }

        this.getRigidBody().setMass(x);
        this.getRigidBody().setBuoyancyType(this.currentBlockState.isIn(PhysicsTags.IS_FLOATING_ON_WATER) ? ElementRigidBody.BuoyancyType.WATER : ElementRigidBody.BuoyancyType.NONE);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put("BlockState", NbtHelper.fromBlockState(this.currentBlockState));
        nbt.putInt("DespawnTimerValue", this.despawnTimerValue);
        nbt.putInt("DespawnTimer", this.despawnTimer);
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    protected void addDebugText(Consumer<Text> consumer) {
        consumer.accept(Text.literal("DespawnTimer: " + this.despawnTimer + "/" +  this.despawnTimerValue));
        consumer.accept(Text.literal("Damage: " + this.calculateDamage(this.getRigidBody().getFrame().getLocationDelta(new Vector3f()))));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setBlockState(NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), nbt.getCompound("BlockState")));
        this.despawnTimerValue = nbt.getInt("DespawnTimerValue");
        this.despawnTimer = nbt.getInt("DespawnTimer");
        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        return super.interactAt(player, hitPos, hand);
    }

    @Override
    public MinecraftShape.Convex createShape() {
        if (this.currentBlockState == null) {
            return ShapeUtil.CUBE;
        }
        return ShapeUtil.getBlockShape(this.currentBlockState, this.getWorld(), BlockPos.ORIGIN);
    }

    @Override
    public void tick() {
        var delta = this.getRigidBody().getFrame().getLocationDelta(new Vector3f());

        if (delta.lengthSquared() > 0.001) {
            var tmp = this.getRigidBody().getFrame().getLocation(new Vector3f(), 0);
            var vec1 = new Vec3d(tmp.x, tmp.y, tmp.z);
            tmp = this.getRigidBody().getFrame().getLocation(tmp, 1);
            var col = ProjectileUtil.getEntityCollision(this.getWorld(), this, vec1, new Vec3d(tmp.x, tmp.y, tmp.z), this.getBoundingBox().stretch(this.getVelocity()).expand(1.0D), Entity::canBeHitByProjectile);

            if (col != null) {
                var d =  this.calculateDamage(delta);

                if (d > 0.2) {
                    var source = this.getOwner() instanceof PlayerEntity player ? this.getWorld().getDamageSources().playerAttack(player) : this.getWorld().getDamageSources().fallingBlock(this);
                    col.getEntity().damage(source, d);
                }
            }
        }

        if (this.holdingPlayer != null) {
            this.despawnTimer = this.despawnTimerValue;
        } else if (this.despawnTimerValue != -1) {
            if (delta.lengthSquared() < 0.005f) {
                this.despawnTimer--;

                if (this.despawnTimer <= 0) {
                    this.discard();
                    var current = this.getWorld().getBlockState(this.getBlockPos());

                    var ownerEntity = this.getOwner() instanceof PlayerEntity player ? player : null;

                    var profile = this.ownerProfile == null ? CommonProtection.UNKNOWN : this.ownerProfile;

                    if ((current.isAir() || current.isIn(BlockTags.REPLACEABLE) || (current.getBlock() instanceof FluidBlock &&
                            (current.getFluidState().isIn(FluidTags.LAVA) || current.getFluidState().isIn(FluidTags.WATER))))
                            && CommonProtection.canBreakBlock(this.getWorld(), this.getBlockPos(), profile, ownerEntity) && CommonProtection.canPlaceBlock(this.getWorld(), this.getBlockPos(), profile, ownerEntity)) {
                        this.getWorld().breakBlock(this.getBlockPos(), true);
                        this.getWorld().setBlockState(this.getBlockPos(), this.currentBlockState);
                    } else {
                        BlockEntity blockEntity = this.currentBlockState.hasBlockEntity() ? this.getWorld().getBlockEntity(this.getBlockPos()) : null;
                        Block.dropStacks(this.currentBlockState, this.getWorld(), this.getBlockPos(), blockEntity, this, ItemStack.EMPTY);
                    }
                    return;
                }
            } else {
                this.despawnTimer = this.despawnTimerValue;
            }
        }

    }

    private float calculateDamage(Vector3f delta) {
        return Math.max((delta.length()) * this.getRigidBody().getMass() * (this.holdingPlayer != null ? 0.15f : 0.28f), 0);
    }

    @Override
    protected org.joml.Vector3f getBaseTranslation() {
        return new org.joml.Vector3f(-0.5f, -0.5f, -0.5f);
    }

    @Override
    protected Box calculateBoundingBox() {
        if (this.getRigidBody() == null) {
            return super.calculateBoundingBox();
        }

        return this.getRigidBody().getCurrentMinecraftBoundingBox();
    }

    public void setDespawnTimer(int i) {
        this.despawnTimerValue = i;
        this.despawnTimer = i;
    }
}
