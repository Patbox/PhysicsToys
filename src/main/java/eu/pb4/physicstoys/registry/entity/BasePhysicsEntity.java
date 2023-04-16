package eu.pb4.physicstoys.registry.entity;

import com.jme3.math.Vector3f;
import com.mojang.authlib.GameProfile;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity.EntitySupplier;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import eu.pb4.physicstoys.registry.item.PhysicsEntityInteractor;
import eu.pb4.physicstoys.registry.item.PhysicsGunItem;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.MarkerElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class BasePhysicsEntity extends Entity implements PolymerEntity, EntityPhysicsElement, Ownable {
    protected final ElementHolder holder = new ElementHolder() {
        @Override
        protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {
        }

        @Override
        protected void startWatchingExtraPackets(ServerPlayNetworkHandler player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
            packetConsumer.accept(new EntityPassengersSetS2CPacket(BasePhysicsEntity.this));
        }
    };

    private final EntityAttachment attachment;
    private UUID owner = null;
    public GameProfile ownerProfile = null;

    protected final DisplayElement mainDisplayElement = this.createMainDisplayElement();
    protected final InteractionElement interactionElement = InteractionElement.redirect(this);
    protected final InteractionElement interactionElement2 = InteractionElement.redirect(this);
    protected PlayerEntity holdingPlayer;


    protected abstract DisplayElement createMainDisplayElement();

    private com.jme3.math.Quaternion storedQuad = new com.jme3.math.Quaternion();

    private EntityRigidBody rigidBody;

    public BasePhysicsEntity(EntityType<?> type, World world) {
        super(type, world);
        this.rigidBody = new EntityRigidBody(this);
        this.rigidBody.setMass(10);
        this.rigidBody.setBuoyancyType(ElementRigidBody.BuoyancyType.WATER);
        this.interactionElement.setSize(1f, 0.5f);
        this.interactionElement2.setSize(1f, -0.5f);

        this.holder.addElement(this.mainDisplayElement);
        this.holder.addElement(this.interactionElement);
        this.holder.addElement(this.interactionElement2);

        this.mainDisplayElement.setInterpolationDuration(type.getTrackTickInterval());

        this.mainDisplayElement.setTranslation(new org.joml.Vector3f(-0.5f, -0.5f, -0.5f));
        VirtualEntityUtils.addVirtualPassenger(this, this.mainDisplayElement.getEntityId());
        VirtualEntityUtils.addVirtualPassenger(this, this.interactionElement.getEntityId());
        VirtualEntityUtils.addVirtualPassenger(this, this.interactionElement2.getEntityId());
        this.attachment = new EntityAttachment(this.holder, this, false);
    }

    @Override
    public boolean sendEmptyTrackerUpdates() {
        return true;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
    }

    @Override
    public void onEntityTrackerTick(Set<EntityTrackingListener> listeners) {
        var rot = Convert.toMinecraft(this.getPhysicsRotation(this.storedQuad, 0));
        var trans = new org.joml.Vector3f(-0.5f, -0.5f, -0.5f).rotate(rot);
        this.mainDisplayElement.setLeftRotation(rot);
        this.mainDisplayElement.setTranslation(trans);

        if (this.mainDisplayElement.isDirty()) {
            this.mainDisplayElement.startInterpolation();
        }
        var vec = this.getPhysicsLocation(new Vector3f(), 0);
        var oldPos = new Vec3d(vec.x, vec.y, vec.z);
        vec = this.getPhysicsLocation(vec, 1);
        var nextPos = new Vec3d(vec.x, vec.y, vec.z);

        var movePacket = VirtualEntityUtils.createMovePacket(this.getId(), oldPos, nextPos, false, 0, 0);
        this.holder.tick();

        if (movePacket != null) {
            for (var x : listeners) {
                x.sendPacket(movePacket);
            }
        }
    }

    @Override
    public EntityRigidBody getRigidBody() {
        return this.rigidBody;
    }

    protected void updateBody() {
        this.rigidBody.setCollisionShape(this.createShape());
        this.setBoundingBox(this.calculateBoundingBox());
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        PolymerEntity.super.onEntityPacketSent(consumer, packet);
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        data.clear();
        if (initial) {
            data.add(DataTracker.SerializedEntry.of(EntityTrackedData.NO_GRAVITY, true));
            data.add(DataTracker.SerializedEntry.of(ArmorStandEntity.ARMOR_STAND_FLAGS, (byte) ArmorStandEntity.MARKER_FLAG));
            data.add(DataTracker.SerializedEntry.of(EntityTrackedData.SILENT, true));
            data.add(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        }
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean canUsePortals() {
        return true;
    }

    @Override
    public boolean skipVanillaEntityCollisions() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        final var box = rigidBody.getCurrentBoundingBox();
        final var location = rigidBody.getPhysicsLocation(new Vector3f()).subtract(new Vector3f(0, -box.getYExtent(), 0));
        final var mass = rigidBody.getMass();

        final var vanillaBox = rigidBody.getCurrentMinecraftBoundingBox();

            final var entityPos = Convert.toBullet(entity.getPos().add(0, entity.getBoundingBox().getYLength(), 0));
            final var normal = location.subtract(entityPos).multLocal(new Vector3f(1, 0, 1)).normalize();

            final var intersection = entity.getBoundingBox().intersection(vanillaBox);
            final var force = normal.clone()
                    .multLocal((float) intersection.getAverageSideLength() / (float) vanillaBox.getAverageSideLength())
                    .multLocal(mass)
                    .multLocal(new Vector3f(1, 0, 1));
            rigidBody.applyCentralImpulse(force);

    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        var x = new Vector3f((float) deltaX, (float) deltaY, (float) deltaZ);
        x.mult(100).mult(this.rigidBody.getMass());
        this.rigidBody.applyCentralImpulse(x);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        var stack = player.getStackInHand(hand);

        if (stack.getItem() instanceof PhysicsEntityInteractor physicsGunItem) {
            physicsGunItem.onInteractWith(player, stack, hitPos, this);
        }

        return super.interactAt(player, hitPos, hand);
    }

    @Override
    public boolean handleAttack(Entity attacker) {

        if (attacker instanceof ServerPlayerEntity player) {
            var stack = player.getMainHandStack();

            if (stack.getItem() instanceof PhysicsEntityInteractor physicsGunItem) {
                physicsGunItem.onAttackWith(player, stack, this);
            }
        }
        return super.handleAttack(attacker);
    }

    @Override
    protected void initDataTracker() {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (this.owner != null) {
            nbt.put("Owner", NbtHelper.fromUuid(this.owner));
        }
        if (this.ownerProfile != null) {
            nbt.put("OwnerProfile", NbtHelper.writeGameProfile(new NbtCompound(), this.ownerProfile));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (nbt.contains("Owner")) {
            this.owner = NbtHelper.toUuid(nbt.getCompound("Owner"));
        }

        if (nbt.contains("OwnerProfile")) {
            this.ownerProfile = NbtHelper.toGameProfile(nbt.getCompound("OwnerProfile"));
        }
    }


    public void setHolder(PlayerEntity player) {
        this.holdingPlayer = player;
        if (player != null) {
            this.setOwner(player.getGameProfile());
        }
    }

    public PlayerEntity getHolder() {
        return this.holdingPlayer;
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return ((ServerWorld) this.world).getEntity(this.owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.ownerProfile = new GameProfile(owner, null);
    }

    public void setOwner(GameProfile ownerProfile) {
        this.owner = ownerProfile.getId();
        this.ownerProfile = ownerProfile;
    }
}
