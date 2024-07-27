package dev.lazurite.rayon.impl.bullet.collision.space.storage;

import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import net.minecraft.world.World;

/**
 * Used for storing a {@link MinecraftSpace} within any
 * {@link World} object.
 */
public interface SpaceStorage {
    void setSpace(MinecraftSpace space);
    MinecraftSpace getSpace();
}