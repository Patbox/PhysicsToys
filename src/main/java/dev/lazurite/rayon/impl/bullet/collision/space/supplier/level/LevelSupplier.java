package dev.lazurite.rayon.impl.bullet.collision.space.supplier.level;

import dev.lazurite.rayon.impl.bullet.thread.PhysicsThread;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * This interface is what allows the {@link PhysicsThread} to retrieve
 * a list of {@link World} objects without knowing where they come from.
 * In this way, it can be used for client Levels or server Levels and,
 * in the case of immersive portals, it can be used to provide multiple
 * client Levels.
 * @see PhysicsThread
 * @see ServerLevelSupplier
 */
public interface LevelSupplier {
     /**
      * Provides the complete list of {@link World}s. If
      * there aren't any, it will return an empty list.
      * @return the list of {@link World}s.
      */
     List<World> getAll();

     /**
      * Provides a specific {@link World} based on the given {@link RegistryKey}.
      * @param key the {@link RegistryKey} to identify the Level with
      * @return a {@link World}
      */
     World get(RegistryKey<World> key);

     /**
      * Provides a specific {@link World} based on the given {@link RegistryKey}.
      * @param key the {@link RegistryKey} to identify the Level with
      * @return an optional {@link World}
      */
     Optional<World> getOptional(RegistryKey<World> key);
}