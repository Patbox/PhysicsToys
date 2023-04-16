package eu.pb4.physicstoys.registry;

import eu.pb4.physicstoys.registry.block.PhysicalTntBlock;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import eu.pb4.physicstoys.registry.item.BaseballBatItem;
import eu.pb4.physicstoys.registry.item.PhysicatorItem;
import eu.pb4.physicstoys.registry.item.PhysicsGunItem;
import eu.pb4.physicstoys.registry.item.PhysicsTntCannonItem;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.physicstoys.PhysicsToysMod;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class USRegistry {
    public static final EntityType<BlockPhysicsEntity> BLOCK_ENTITY = register("block", FabricEntityTypeBuilder.create(SpawnGroup.MISC, BlockPhysicsEntity::new).trackRangeChunks(7).trackedUpdateRate(1).build(), Registries.ENTITY_TYPE);
    public static final EntityType<PhysicalTntEntity> TNT_ENTITY = register("tnt", FabricEntityTypeBuilder.create(SpawnGroup.MISC,  PhysicalTntEntity::new).trackRangeChunks(7).trackedUpdateRate(1).build(), Registries.ENTITY_TYPE);

    public static final Item PHYSICATOR_ITEM = register("physicator", new PhysicatorItem(new Item.Settings().maxCount(1)), Registries.ITEM);
    public static final Item PHYSICS_GUN_ITEM = register("gravity_gun", new PhysicsGunItem(new Item.Settings().maxCount(1)), Registries.ITEM);
    public static final Item BASEBALL_BAT_ITEM = register("baseball_bat", new BaseballBatItem(new Item.Settings().maxCount(1)), Registries.ITEM);
    public static final PhysicalTntBlock PHYSICAL_TNT_BLOCK = register("tnt", new PhysicalTntBlock(AbstractBlock.Settings.copy(Blocks.TNT)), Registries.BLOCK);
    public static final Item PHYSICAL_TNT_ITEM = register("tnt", new PolymerBlockItem(PHYSICAL_TNT_BLOCK, new Item.Settings(), Items.TNT) {
        @Override
        public boolean hasGlint(ItemStack stack) {
            return true;
        }
    }, Registries.ITEM);

    public static final PhysicsTntCannonItem TNT_CANNON_ITEM = register("tnt_cannon", new PhysicsTntCannonItem(new Item.Settings().maxCount(1)), Registries.ITEM);


    public static ItemGroup ITEM_GROUP = PolymerItemGroupUtils.builder(PhysicsToysMod.id("item_group"))
            .icon(() -> Items.APPLE.getDefaultStack())
            .displayName(Text.translatable("itemGroup.physics_toys"))
            .entries(((displayContext, entries) -> {
                entries.add(PHYSICS_GUN_ITEM);
                entries.add(BASEBALL_BAT_ITEM);
                entries.add(TNT_CANNON_ITEM);
                entries.add(PHYSICAL_TNT_ITEM);
                entries.add(PHYSICATOR_ITEM);
            }))
            .build();

    public static <A extends T, T> A register(String key, A value, Registry<T> registry) {
        if (value instanceof BlockEntityType<?> blockEntityType) {
            PolymerBlockUtils.registerBlockEntity(blockEntityType);
        } else if (value instanceof EntityType<?> entityType) {
            PolymerEntityUtils.registerType(entityType);
        }
        return Registry.register(registry, PhysicsToysMod.id(key), value);
    }

    public static void register() {

    };
}
