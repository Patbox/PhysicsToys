package eu.pb4.physicstoys.registry.entity;

import com.jme3.math.Vector3f;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.rayon.api.EntityPhysicsElement;
import eu.pb4.rayon.impl.bullet.collision.body.ElementRigidBody;
import eu.pb4.rayon.impl.bullet.collision.body.EntityRigidBody;
import eu.pb4.rayon.impl.bullet.math.Convert;
import eu.pb4.physicstoys.PhysicsToysMod;
import eu.pb4.physicstoys.registry.item.PhysicsEntityInteractor;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Brightness;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class BasePhysicsEntity extends Entity implements PolymerEntity, EntityPhysicsElement, TraceableEntity {
    protected final ElementHolder holder = new ElementHolder() {
        @Override
        protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
        }

        @Override
        protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
            packetConsumer.accept(new ClientboundSetPassengersPacket(BasePhysicsEntity.this));
        }
    };

    private final EntityAttachment attachment;
    private UUID owner = null;
    public NameAndId ownerProfile = null;

    protected final DisplayElement mainDisplayElement = this.createMainDisplayElement();
    protected final InteractionElement interactionElement = InteractionElement.redirect(this);
    protected final InteractionElement interactionElement2 = InteractionElement.redirect(this);
    private final TextDisplayElement debugText;
    protected Player holdingPlayer;


    protected abstract DisplayElement createMainDisplayElement();

    private com.jme3.math.Quaternion storedQuad = new com.jme3.math.Quaternion();

    private EntityRigidBody rigidBody;

    public BasePhysicsEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.rigidBody = new EntityRigidBody(this);
        this.rigidBody.setMass(10);
        this.rigidBody.setBuoyancyType(ElementRigidBody.BuoyancyType.WATER);
        var w = this.getInteractionWidth();
        var h = this.getInteractionHeight() / 2;
        this.interactionElement.setSize(w, h);
        this.interactionElement2.setSize(w, -h);
        this.mainDisplayElement.ignorePositionUpdates();
        this.interactionElement.ignorePositionUpdates();
        this.interactionElement2.ignorePositionUpdates();

        this.holder.addElement(this.mainDisplayElement);
        this.holder.addElement(this.interactionElement);
        this.holder.addElement(this.interactionElement2);

        if (PhysicsToysMod.IS_DEV && false) {
            this.debugText = new TextDisplayElement();
            this.debugText.setDisplayHeight(5);
            this.debugText.setDisplayWidth(5);
            this.debugText.setBillboardMode(Display.BillboardConstraints.CENTER);
            this.debugText.setBrightness(new Brightness(15, 15));
            this.debugText.setTranslation(new org.joml.Vector3f(0, h + 0.2f, 0));
            VirtualEntityUtils.addVirtualPassenger(this, this.debugText.getEntityId());
            this.holder.addElement(this.debugText);
        } else {
            this.debugText = null;
        }

        this.mainDisplayElement.setInterpolationDuration(type.updateInterval());
        this.mainDisplayElement.setTeleportDuration(type.updateInterval());

        this.mainDisplayElement.setTranslation(new org.joml.Vector3f(-0.5f, -0.5f, -0.5f));
        VirtualEntityUtils.addVirtualPassenger(this, this.mainDisplayElement.getEntityId());
        VirtualEntityUtils.addVirtualPassenger(this, this.interactionElement.getEntityId());
        VirtualEntityUtils.addVirtualPassenger(this, this.interactionElement2.getEntityId());
        this.attachment = new EntityAttachment(this.holder, this, false);
    }

    protected abstract float getInteractionWidth();

    protected abstract float getInteractionHeight();

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
    }

    @Override
    public void onEntityTrackerTick(Set<ServerPlayerConnection> listeners) {
        var rot = Convert.toMinecraft(this.getPhysicsRotation(this.storedQuad, 0));
        var trans = this.getBaseTranslation().rotate(rot);
        this.mainDisplayElement.setLeftRotation(rot);
        this.mainDisplayElement.setTranslation(trans);

        if (this.debugText != null) {
            var dbg = Component.empty()
                    .append(Component.literal("Mass: " + this.getRigidBody().getMass()))
                    .append(Component.literal("\nBuoyancy: " + this.getRigidBody().getBuoyancyType().name()));

            this.addDebugText((t) -> dbg.append("\n").append(t));

            this.debugText.setText(dbg);
        }

        if (this.mainDisplayElement.isDirty()) {
            this.mainDisplayElement.startInterpolation();
        }

        this.holder.tick();
    }

    protected org.joml.Vector3f getBaseTranslation() {
        return new org.joml.Vector3f();
    }

    protected void addDebugText(Consumer<Component> consumer) {
    }

    @Override
    public EntityRigidBody getRigidBody() {
        return this.rigidBody;
    }

    protected void updateBody() {
        this.rigidBody.setCollisionShape(this.createShape());
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        var empty = data.isEmpty();
        data.clear();
        if (initial || empty) {
            //data.add(DataTracker.SerializedEntry.of(EntityEntityData.NO_GRAVITY, true));
            //data.add(DataTracker.SerializedEntry.of(ArmorStandEntity.ARMOR_STAND_FLAGS, (byte) ArmorStandEntity.MARKER_FLAG));
            //data.add(DataTracker.SerializedEntry.of(EntityEntityData.SILENT, true));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.HEIGHT, 0f));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.WIDTH, 0f));
            data.add(SynchedEntityData.DataValue.create(DisplayEntityData.TELEPORTATION_DURATION, this.getType().updateInterval()));
            data.add(SynchedEntityData.DataValue.create(EntityData.FLAGS, (byte) (1 << EntityData.INVISIBLE_FLAG_INDEX)));
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean canUsePortal(boolean allowVehicles) {
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
    public void push(Entity entity) {
        final var box = rigidBody.getCurrentBoundingBox();
        final var location = rigidBody.getPhysicsLocation(new Vector3f()).subtract(new Vector3f(0, -box.getYExtent(), 0));
        final var mass = rigidBody.getMass();

        final var vanillaBox = rigidBody.getCurrentMinecraftBoundingBox();

            final var entityPos = Convert.toBullet(entity.position().add(0, entity.getBoundingBox().getYsize(), 0));
            final var normal = location.subtract(entityPos).multLocal(new Vector3f(1, 0, 1)).normalize();

            final var intersection = entity.getBoundingBox().intersect(vanillaBox);
            final var force = normal.clone()
                    .multLocal((float) intersection.getSize() / (float) vanillaBox.getSize())
                    .multLocal(mass)
                    .multLocal(new Vector3f(1, 0, 1));
            rigidBody.applyCentralImpulse(force);

    }

    @Override
    public void push(double deltaX, double deltaY, double deltaZ) {
        var x = new Vector3f((float) deltaX, (float) deltaY, (float) deltaZ);
        x.mult(100).mult(this.rigidBody.getMass());
        this.rigidBody.applyCentralImpulse(x);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        var stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof PhysicsEntityInteractor physicsGunItem) {
            physicsGunItem.onInteractWith(player, stack, location, this);
        }

        return super.interact(player, hand, location);
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {

        if (attacker instanceof ServerPlayer player) {
            var stack = player.getMainHandItem();

            if (stack.getItem() instanceof PhysicsEntityInteractor physicsGunItem) {
                physicsGunItem.onAttackWith(player, stack, this);
            } else {
                var x = EnchantmentHelper.getItemEnchantmentLevel(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK), stack);
                this.getRigidBody().applyCentralImpulse(Convert.toBullet(player.getViewVector(0)).mult((x + 1) * 30));
            }
        }
        return super.skipAttackInteraction(attacker);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        this.owner = view.read("Owner", UUIDUtil.LENIENT_CODEC).orElse(null);
        this.ownerProfile = view.read("OwnerProfile", NameAndId.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        if (this.owner != null) {
            view.store("Owner", UUIDUtil.AUTHLIB_CODEC, this.owner);
        }
        if (this.ownerProfile != null) {
            view.store("OwnerProfile", NameAndId.CODEC, this.ownerProfile);
        }
    }


    public void setHolder(Player player) {
        this.holdingPlayer = player;
        if (player != null) {
            this.setOwner(player.getGameProfile());
        }
    }

    public Player getHolder() {
        return this.holdingPlayer;
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return ((ServerLevel) this.level()).getEntity(this.owner);
    }

    public void setOwner(GameProfile ownerProfile) {
        this.owner = ownerProfile.id();
        this.ownerProfile = new NameAndId(ownerProfile);
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }
}
