package eu.pb4.rayon.impl.bullet.collision.space.supplier.level;

import eu.pb4.rayon.impl.bullet.thread.PhysicsThread;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * This interface is what allows the {@link PhysicsThread} to retrieve
 * a list of {@link Level} objects without knowing where they come from.
 * In this way, it can be used for client Levels or server Levels and,
 * in the case of immersive portals, it can be used to provide multiple
 * client Levels.
 * @see PhysicsThread
 * @see ServerLevelSupplier
 */
public interface LevelSupplier {
     /**
      * Provides the complete list of {@link Level}s. If
      * there aren't any, it will return an empty list.
      * @return the list of {@link Level}s.
      */
     List<Level> getAll();

     /**
      * Provides a specific {@link Level} based on the given {@link ResourceKey}.
      * @param key the {@link ResourceKey} to identify the Level with
      * @return a {@link Level}
      */
     Level get(ResourceKey<Level> key);

     /**
      * Provides a specific {@link Level} based on the given {@link ResourceKey}.
      * @param key the {@link ResourceKey} to identify the Level with
      * @return an optional {@link Level}
      */
     Optional<Level> getOptional(ResourceKey<Level> key);
}