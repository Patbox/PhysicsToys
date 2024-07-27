package dev.lazurite.rayon.impl.bullet.collision.space.supplier.level;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This {@link LevelSupplier} provides a list of all
 * {@link ServerWorld} objects running on the {@link MinecraftServer}.
 */
public record ServerLevelSupplier(MinecraftServer server) implements LevelSupplier {
    @Override
    public List<World> getAll() {
        return new ArrayList<>((Collection<? extends World>) server.getWorlds());
    }

    @Override
    public World get(RegistryKey<World> key) {
        return server.getWorld(key);
    }

    @Override
    public Optional<World> getOptional(RegistryKey<World> key) {
        return Optional.ofNullable(get(key));
    }
}