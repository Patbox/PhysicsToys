package eu.pb4.physicstoys.registry;

import eu.pb4.physicstoys.PhysicsToysMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class PhysicsTags {
    public static final TagKey<Block> IS_FLOATING_ON_WATER = TagKey.create(Registries.BLOCK, PhysicsToysMod.id("float_in_water"));
}
