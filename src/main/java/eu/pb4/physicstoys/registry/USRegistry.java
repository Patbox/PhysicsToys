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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;

import java.util.UUID;
import java.util.function.Function;

public class USRegistry {
    public static final EntityType<BlockPhysicsEntity> BLOCK_ENTITY = register("block", (key) -> EntityType.Builder.create(BlockPhysicsEntity::new, SpawnGroup.MISC).maxTrackingRange(7).trackingTickInterval(1).build(key), Registries.ENTITY_TYPE);
    public static final EntityType<PhysicalTntEntity> TNT_ENTITY = register("tnt", (key) -> EntityType.Builder.create(PhysicalTntEntity::new, SpawnGroup.MISC).maxTrackingRange(7).trackingTickInterval(1).build(key), Registries.ENTITY_TYPE);

    public static final Item PHYSICATOR_ITEM = register("physicator", (key) -> new PhysicatorItem(new Item.Settings().registryKey(key).maxCount(1)), Registries.ITEM);
    public static final Item PHYSICS_GUN_ITEM = register("gravity_gun", (key) -> new PhysicsGunItem(new Item.Settings().registryKey(key).maxCount(1)), Registries.ITEM);
    public static final Item BASEBALL_BAT_ITEM = register("baseball_bat", (key) -> new BaseballBatItem(new Item.Settings().registryKey(key).maxCount(1)), Registries.ITEM);
    public static final PhysicalTntBlock PHYSICAL_TNT_BLOCK = register("tnt", (key) -> new PhysicalTntBlock(AbstractBlock.Settings.copy(Blocks.TNT).registryKey(key)), Registries.BLOCK);
    public static final Item PHYSICAL_TNT_ITEM = register("tnt", (key) -> new PolymerBlockItem(PHYSICAL_TNT_BLOCK, new Item.Settings().registryKey(key).useBlockPrefixedTranslationKey(), Items.TNT) {
        @Override
        public boolean hasGlint(ItemStack stack) {
            return true;
        }
    }, Registries.ITEM);

    public static final PhysicsTntCannonItem TNT_CANNON_ITEM = register("tnt_cannon", (key) -> new PhysicsTntCannonItem(new Item.Settings().registryKey(key).maxCount(1)), Registries.ITEM);


    public static ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .icon(Items.APPLE::getDefaultStack)
            .displayName(Text.translatable("itemGroup.physics_toys"))
            .entries((displayContext, entries) -> {
                entries.add(PHYSICS_GUN_ITEM);
                entries.add(BASEBALL_BAT_ITEM);
                entries.add(TNT_CANNON_ITEM);
                entries.add(PHYSICAL_TNT_ITEM);
                entries.add(PHYSICATOR_ITEM);
            })
            .build();

    public static final ComponentType<UUID> TARGET_COMPONENT = register("held_entity", (key) -> ComponentType.<UUID>builder().codec(Uuids.CODEC).packetCodec(Uuids.PACKET_CODEC).build(), Registries.DATA_COMPONENT_TYPE);
    public static final ComponentType<Long> PICK_TIME_COMPONENT = register("pick_time", (key) -> ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build(), Registries.DATA_COMPONENT_TYPE);

    public static <A extends T, T> A register(String key, Function<RegistryKey<T>, A> function, Registry<T> registry) {
        var id = PhysicsToysMod.id(key);
        var value = function.apply(RegistryKey.of(registry.getKey(), id));
        if (value instanceof BlockEntityType<?> blockEntityType) {
            PolymerBlockUtils.registerBlockEntity(blockEntityType);
        } else if (value instanceof EntityType<?> entityType) {
            PolymerEntityUtils.registerType(entityType);
        } else if (value instanceof ComponentType<?> componentType) {
            PolymerComponent.registerDataComponent(componentType);
        }
        return Registry.register(registry, id, value);
    }

    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(PhysicsToysMod.id("item_group"), ITEM_GROUP);
    }
}
