package eu.pb4.physicstoys.datagen;

import eu.pb4.physicstoys.registry.PhysicsTags;
import eu.pb4.physicstoys.registry.USRegistry;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class DataGenInit implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();

        pack.addProvider(CBlockTags::new);
        pack.addProvider(LootTables::new);
        pack.addProvider(Recipes::new);
    }

    private static class CBlockTags extends FabricTagsProvider.BlockTagsProvider {
        public CBlockTags(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider arg) {
            this.valueLookupBuilder(PhysicsTags.IS_FLOATING_ON_WATER)
                    .addOptionalTag(BlockTags.REPLACEABLE)
                    .addOptionalTag(BlockTags.PLANKS)
                    .addOptionalTag(BlockTags.LOGS)
                    .addOptionalTag(BlockTags.LEAVES)
                    .addOptionalTag(BlockTags.WOOL)
                    .addOptionalTag(BlockTags.CAMPFIRES)
                    .addOptionalTag(BlockTags.WOOL_CARPETS)
                    .addOptionalTag(BlockTags.SAPLINGS)
                    .addOptionalTag(BlockTags.CROPS)
                    .addOptionalTag(BlockTags.ALL_SIGNS)
                    .addOptionalTag(BlockTags.SNOW)
                    .addOptionalTag(BlockTags.BANNERS)
                    .addOptionalTag(BlockTags.FLOWERS)
                    .addOptionalTag(BlockTags.WOODEN_BUTTONS)
                    .addOptionalTag(BlockTags.WOODEN_DOORS)
                    .addOptionalTag(BlockTags.WOODEN_FENCES)
                    .addOptionalTag(BlockTags.WOODEN_PRESSURE_PLATES)
                    .addOptionalTag(BlockTags.WOODEN_SLABS)
                    .addOptionalTag(BlockTags.WOODEN_STAIRS)
                    .addOptionalTag(BlockTags.WOODEN_TRAPDOORS)
                    .addOptionalTag(BlockTags.BEEHIVES)
                    .add(Blocks.BAMBOO)
                    .add(Blocks.NOTE_BLOCK)
                    .add(Blocks.JUKEBOX)
                    .add(Blocks.DEAD_BUSH)
            ;
        }
    }

    private static class LootTables extends FabricBlockLootSubProvider {
        protected LootTables(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generate() {
            this.dropSelf(USRegistry.PHYSICAL_TNT_BLOCK);
        }
    }

    private static class Recipes extends FabricRecipeProvider {
        public Recipes(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
            return new RecipeProvider(registryLookup, exporter) {
                @Override
                public void buildRecipes() {
                    var itemWrap = registryLookup.lookupOrThrow(Registries.ITEM);
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.REDSTONE, USRegistry.PHYSICS_GUN_ITEM)
                            .unlockedBy("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHER_STAR))
                            .pattern("pia")
                            .pattern("p  ")
                            .define('a', Items.ENDER_EYE)
                            .define('p', Items.IRON_INGOT)
                            .define('i', Items.NETHER_STAR)
                            .save(output);

                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.REDSTONE, USRegistry.TNT_CANNON_ITEM)
                            .unlockedBy("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(USRegistry.PHYSICS_GUN_ITEM))
                            .pattern("p ")
                            .pattern("i ")
                            .define('p', Items.COPPER_INGOT)
                            .define('i', USRegistry.PHYSICS_GUN_ITEM)
                            .save(output);

                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.REDSTONE, USRegistry.BASEBALL_BAT_ITEM)
                            .unlockedBy("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(USRegistry.PHYSICS_GUN_ITEM))
                            .pattern(" p ")
                            .pattern("ipi")
                            .pattern(" i ")
                            .define('p', ItemTags.PLANKS)
                            .define('i', Items.STICK)
                            .save(output);

                    ShapelessRecipeBuilder.shapeless(itemWrap, RecipeCategory.REDSTONE, USRegistry.PHYSICAL_TNT_ITEM, 8)
                            .unlockedBy("get_item", InventoryChangeTrigger.TriggerInstance.hasItems(Items.TNT))
                            .requires(Items.ENDER_EYE)
                            .requires(Items.TNT, 8)
                            .save(output);

                }
            };
        }

        @Override
        public String getName() {
            return "recipe";
        }
    }
}
