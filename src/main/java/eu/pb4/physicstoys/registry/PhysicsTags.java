package eu.pb4.physicstoys.registry;

import eu.pb4.physicstoys.PhysicsToysMod;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class PhysicsTags {
    public static TagKey<Block> IS_FLOATING_ON_WATER = TagKey.of(RegistryKeys.BLOCK, PhysicsToysMod.id("float_in_water"));
}
