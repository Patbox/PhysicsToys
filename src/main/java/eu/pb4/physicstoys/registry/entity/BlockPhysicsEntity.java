package eu.pb4.physicstoys.registry.entity;

import com.jme3.math.Vector3f;
import com.mojang.authlib.GameProfile;
import eu.pb4.rayon.impl.bullet.collision.body.ElementRigidBody;
import eu.pb4.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import eu.pb4.rayon.impl.bullet.math.Convert;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.physicstoys.other.ShapeUtil;
import eu.pb4.physicstoys.registry.PhysicsTags;
import eu.pb4.physicstoys.registry.USRegistry;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlockPhysicsEntity extends BasePhysicsEntity {
    private BlockState currentBlockState = Blocks.AIR.defaultBlockState();
    private int despawnTimerValue = -1;
    private int despawnTimer;

    public BlockPhysicsEntity(EntityType<BlockPhysicsEntity> type, Level world) {
        super(type, world);
    }

    public static BlockPhysicsEntity create(Level world, BlockState state, BlockPos pos) {
        var entity = new BlockPhysicsEntity(USRegistry.BLOCK_ENTITY, world);
        var vec = Vec3.atCenterOf(pos);
        entity.setBlockState(state);
        entity.setPos(vec.x, vec.y, vec.z);
        entity.getRigidBody().setLinearVelocity(new Vector3f());
        entity.getRigidBody().setAngularVelocity(new Vector3f());
        entity.getRigidBody().setPhysicsLocation(Convert.toBullet(entity.position()));
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
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return false;
    }

    @Override
    protected Component getTypeName() {
        return this.currentBlockState.getBlock().getName();
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
        var x = (float) Math.min(Math.max(12f * Math.log1p(Math.max(this.currentBlockState.getBlock().defaultDestroyTime(), this.currentBlockState.getBlock().getExplosionResistance())), 5), 50);

        if (x < 0 || Float.isNaN(x)) {
            x = 60;
        }

        this.getRigidBody().setMass(x);
        this.getRigidBody().setBuoyancyType(this.currentBlockState.is(PhysicsTags.IS_FLOATING_ON_WATER) ? ElementRigidBody.BuoyancyType.WATER : ElementRigidBody.BuoyancyType.NONE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        view.store("BlockState", CompoundTag.CODEC, NbtUtils.writeBlockState(this.currentBlockState));
        view.putInt("DespawnTimerValue", this.despawnTimerValue);
        view.putInt("DespawnTimer", this.despawnTimer);
        super.addAdditionalSaveData(view);
    }

    @Override
    protected void addDebugText(Consumer<Component> consumer) {
        consumer.accept(Component.literal("DespawnTimer: " + this.despawnTimer + "/" +  this.despawnTimerValue));
        consumer.accept(Component.literal("Damage: " + this.calculateDamage(this.getRigidBody().getFrame().getLocationDelta(new Vector3f()))));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {

        view.read("BlockState", CompoundTag.CODEC).ifPresent(x ->this.setBlockState(NbtUtils.readBlockState(BuiltInRegistries.BLOCK, x)));
        this.despawnTimerValue = view.getIntOr("DespawnTimerValue", 0);
        this.despawnTimer = view.getIntOr("DespawnTimer", 0);
        super.readAdditionalSaveData(view);
    }

    @Override
    public MinecraftShape.Convex createShape() {
        if (this.currentBlockState == null) {
            return ShapeUtil.CUBE;
        }
        return ShapeUtil.getBlockShape(this.currentBlockState, this.level(), BlockPos.ZERO);
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel world) {
            var delta = this.getRigidBody().getFrame().getLocationDelta(new Vector3f());

            if (delta.lengthSquared() > 0.001) {
                var tmp = this.getRigidBody().getFrame().getLocation(new Vector3f(), 0);
                var vec1 = new Vec3(tmp.x, tmp.y, tmp.z);
                tmp = this.getRigidBody().getFrame().getLocation(tmp, 1);
                var col = ProjectileUtil.getEntityHitResult(this.level(), this, vec1, new Vec3(tmp.x, tmp.y, tmp.z), this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), Entity::canBeHitByProjectile,0.03f);

                if (col != null) {
                    var d = this.calculateDamage(delta);

                    if (d > 0.2) {
                        var source = this.getOwner() instanceof Player player ? this.level().damageSources().playerAttack(player) : this.level().damageSources().fallingBlock(this);
                        col.getEntity().hurtServer(world, source, d);
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
                        var current = this.level().getBlockState(this.blockPosition());

                        var ownerEntity = this.getOwner() instanceof Player player ? player : null;

                        var profile = this.ownerProfile == null ? CommonProtection.UNKNOWN : this.ownerProfile;

                        if ((current.isAir() || current.is(BlockTags.REPLACEABLE) || (current.getBlock() instanceof LiquidBlock &&
                                (current.getFluidState().is(FluidTags.LAVA) || current.getFluidState().is(FluidTags.WATER))))
                                && CommonProtection.canBreakBlock(this.level(), this.blockPosition(), profile, ownerEntity) && CommonProtection.canPlaceBlock(this.level(), this.blockPosition(), profile, ownerEntity)) {
                            this.level().destroyBlock(this.blockPosition(), true);
                            this.level().setBlockAndUpdate(this.blockPosition(), this.currentBlockState);
                        } else {
                            BlockEntity blockEntity = this.currentBlockState.hasBlockEntity() ? this.level().getBlockEntity(this.blockPosition()) : null;
                            Block.dropResources(this.currentBlockState, this.level(), this.blockPosition(), blockEntity, this, ItemStack.EMPTY);
                        }
                        return;
                    }
                } else {
                    this.despawnTimer = this.despawnTimerValue;
                }
            }
        }
        super.tick();
    }

    private float calculateDamage(Vector3f delta) {
        return Math.max((delta.length()) * this.getRigidBody().getMass() * (this.holdingPlayer != null ? 0.15f : 0.28f), 0);
    }

    @Override
    protected org.joml.Vector3f getBaseTranslation() {
        return new org.joml.Vector3f(-0.5f, -0.5f, -0.5f);
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        if (this.getRigidBody() == null) {
            return super.makeBoundingBox(pos);
        }
        return this.getRigidBody().getCurrentMinecraftBoundingBox();
    }

    public void setDespawnTimer(int i) {
        this.despawnTimerValue = i;
        this.despawnTimer = i;
    }
}
