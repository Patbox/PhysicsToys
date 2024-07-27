package dev.lazurite.rayon.impl.event;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.api.event.collision.PhysicsSpaceEvents;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.EntityCollisionGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.PressureGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.TerrainGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.storage.SpaceStorage;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity.ServerEntitySupplier;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.level.ServerLevelSupplier;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThread;
import dev.lazurite.toolbox.api.event.ServerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ServerEventHandler {
    private static PhysicsThread thread;

    public static PhysicsThread getThread() {
        return thread;
    }

    public static void register() {
        // Rayon Events
        PhysicsSpaceEvents.STEP.register(PressureGenerator::step);
        PhysicsSpaceEvents.STEP.register(TerrainGenerator::step);
        PhysicsSpaceEvents.ELEMENT_ADDED.register(ServerEventHandler::onElementAddedToSpace);

        // Server Events
        ServerLifecycleEvents.SERVER_STARTING.register(ServerEventHandler::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerEventHandler::onServerStop);
        ServerTickEvents.END_SERVER_TICK.register(ServerEventHandler::onServerTick);

        // Level Events
        ServerWorldEvents.LOAD.register(ServerEventHandler::onLevelLoad);
        ServerTickEvents.START_WORLD_TICK.register(ServerEventHandler::onStartLevelTick);
        ServerTickEvents.START_WORLD_TICK.register(ServerEventHandler::onEntityStartLevelTick);
        ServerEvents.Block.BLOCK_UPDATE.register(ServerEventHandler::onBlockUpdate);

        // Entity Events
        ServerEntityEvents.ENTITY_LOAD.register(ServerEventHandler::onEntityLoad);
        EntityTrackingEvents.START_TRACKING.register(ServerEventHandler::onStartTrackingEntity);
        EntityTrackingEvents.STOP_TRACKING.register(ServerEventHandler::onStopTrackingEntity);
    }

    public static void onBlockUpdate(World level, BlockState blockState, BlockPos blockPos) {
        MinecraftSpace.getOptional(level).ifPresent(space -> space.doBlockUpdate(blockPos));
    }

    public static void onServerStart(MinecraftServer server) {
        thread = new PhysicsThread(server, Thread.currentThread(), new ServerLevelSupplier(server), new ServerEntitySupplier(), "Server Physics Thread");
    }

    public static void onServerStop(MinecraftServer server) {
        thread.destroy();
    }

    public static void onServerTick(MinecraftServer server) {
        if (thread.throwable != null) {
            throw new RuntimeException(thread.throwable);
        }
    }

    public static void onStartLevelTick(World level) {
        MinecraftSpace.get(level).step();
    }

    public static void onLevelLoad(MinecraftServer server, ServerWorld level) {
        final var space = new MinecraftSpace(thread, level);
        ((SpaceStorage) level).setSpace(space);
        PhysicsSpaceEvents.INIT.invoke(space);
    }

    public static void onElementAddedToSpace(MinecraftSpace space, ElementRigidBody rigidBody) {
        if (rigidBody instanceof EntityRigidBody entityBody) {
            final var pos = entityBody.getElement().cast().getPos();
            entityBody.setPhysicsLocation(Convert.toBullet(pos));
        }
    }

    public static void onEntityLoad(Entity entity, World world) {
        if (EntityPhysicsElement.is(entity) && !PlayerLookup.tracking(entity).isEmpty()) {
            var space = MinecraftSpace.get(entity.getWorld());
            space.getWorkerThread().execute(() -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
        }
    }

    public static void onStartTrackingEntity(Entity entity, ServerPlayerEntity player) {
        if (EntityPhysicsElement.is(entity)) {
            var space = MinecraftSpace.get(entity.getWorld());
            space.getWorkerThread().execute(() -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
        }
    }

    public static void onStopTrackingEntity(Entity entity, ServerPlayerEntity player) {
        if (EntityPhysicsElement.is(entity) && PlayerLookup.tracking(entity).isEmpty()) {
            var space = MinecraftSpace.get(entity.getWorld());
            space.getWorkerThread().execute(() -> space.removeCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
        }
    }

    public static void onEntityStartLevelTick(World level) {
        var space = MinecraftSpace.get(level);
        EntityCollisionGenerator.step(space);

        for (var rigidBody : space.getRigidBodiesByClass(EntityRigidBody.class)) {
            /* Set entity position */
            var location = rigidBody.getFrame().getLocation(new Vector3f(), 1.0f);
            rigidBody.getElement().cast().updatePosition(location.x, location.y, location.z);
        }
    }
}