package eu.pb4.physicstoys.registry;

import com.mojang.serialization.Codec;
import eu.pb4.physicstoys.PhysicsToysMod;
import eu.pb4.physicstoys.registry.block.PhysicalTntBlock;
import eu.pb4.physicstoys.registry.entity.BlockPhysicsEntity;
import eu.pb4.physicstoys.registry.entity.PhysicalTntEntity;
import eu.pb4.physicstoys.registry.item.BaseballBatItem;
import eu.pb4.physicstoys.registry.item.PhysicatorItem;
import eu.pb4.physicstoys.registry.item.PhysicsGunItem;
import eu.pb4.physicstoys.registry.item.PhysicsTntCannonItem;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.UUID;
import java.util.function.Function;

public class USRegistry {
    public static final EntityType<BlockPhysicsEntity> BLOCK_ENTITY = register("block", (key) -> EntityType.Builder.of(BlockPhysicsEntity::new, MobCategory.MISC).clientTrackingRange(7).updateInterval(1).build(key), BuiltInRegistries.ENTITY_TYPE);
    public static final EntityType<PhysicalTntEntity> TNT_ENTITY = register("tnt", (key) -> EntityType.Builder.of(PhysicalTntEntity::new, MobCategory.MISC).clientTrackingRange(7).updateInterval(1).build(key), BuiltInRegistries.ENTITY_TYPE);

    public static final Item PHYSICATOR_ITEM = register("physicator", (key) -> new PhysicatorItem(new Item.Properties().setId(key).stacksTo(1)), BuiltInRegistries.ITEM);
    public static final Item PHYSICS_GUN_ITEM = register("gravity_gun", (key) -> new PhysicsGunItem(new Item.Properties().setId(key).stacksTo(1)), BuiltInRegistries.ITEM);
    public static final Item BASEBALL_BAT_ITEM = register("baseball_bat", (key) -> new BaseballBatItem(new Item.Properties().setId(key).stacksTo(1)), BuiltInRegistries.ITEM);
    public static final PhysicalTntBlock PHYSICAL_TNT_BLOCK = register("tnt", (key) -> new PhysicalTntBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TNT).setId(key)), BuiltInRegistries.BLOCK);
    public static final Item PHYSICAL_TNT_ITEM = register("tnt", (key) -> new PolymerBlockItem(PHYSICAL_TNT_BLOCK, new Item.Properties().setId(key).useBlockDescriptionPrefix(), Items.TNT) {
        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }
    }, BuiltInRegistries.ITEM);

    public static final PhysicsTntCannonItem TNT_CANNON_ITEM = register("tnt_cannon", (key) -> new PhysicsTntCannonItem(new Item.Properties().setId(key).stacksTo(1)), BuiltInRegistries.ITEM);


    public static CreativeModeTab ITEM_GROUP = FabricItemGroup.builder()
            .icon(Items.APPLE::getDefaultInstance)
            .title(Component.translatable("itemGroup.physics_toys"))
            .displayItems((displayContext, entries) -> {
                entries.accept(PHYSICS_GUN_ITEM);
                entries.accept(BASEBALL_BAT_ITEM);
                entries.accept(TNT_CANNON_ITEM);
                entries.accept(PHYSICAL_TNT_ITEM);
                entries.accept(PHYSICATOR_ITEM);
            })
            .build();

    public static final DataComponentType<UUID> TARGET_COMPONENT = register("held_entity", (key) -> DataComponentType.<UUID>builder().persistent(UUIDUtil.AUTHLIB_CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build(), BuiltInRegistries.DATA_COMPONENT_TYPE);
    public static final DataComponentType<Long> PICK_TIME_COMPONENT = register("pick_time", (key) -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build(), BuiltInRegistries.DATA_COMPONENT_TYPE);

    public static <A extends T, T> A register(String key, Function<ResourceKey<T>, A> function, Registry<T> registry) {
        var id = PhysicsToysMod.id(key);
        var value = function.apply(ResourceKey.create(registry.key(), id));
        if (value instanceof BlockEntityType<?> blockEntityType) {
            PolymerBlockUtils.registerBlockEntity(blockEntityType);
        } else if (value instanceof EntityType<?> entityType) {
            PolymerEntityUtils.registerType(entityType);
        } else if (value instanceof DataComponentType<?> componentType) {
            PolymerComponent.registerDataComponent(componentType);
        }
        return Registry.register(registry, id, value);
    }

    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(PhysicsToysMod.id("item_group"), ITEM_GROUP);
    }
}
