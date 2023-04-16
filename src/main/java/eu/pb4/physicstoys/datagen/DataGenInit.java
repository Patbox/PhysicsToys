package eu.pb4.physicstoys.datagen;

import eu.pb4.physicstoys.registry.PhysicsTags;
import eu.pb4.physicstoys.registry.USRegistry;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DataGenInit implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();

        pack.addProvider(CBlockTags::new);
        pack.addProvider(LootTables::new);
        pack.addProvider(Recipes::new);
    }

    class CBlockTags extends FabricTagProvider.BlockTagProvider {
        public CBlockTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            this.getOrCreateTagBuilder(PhysicsTags.IS_FLOATING_ON_WATER)
                    .addOptionalTag(BlockTags.REPLACEABLE_PLANTS)
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

    class LootTables extends FabricBlockLootTableProvider {
        protected LootTables(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generate() {
            this.addDrop(USRegistry.PHYSICAL_TNT_BLOCK);
        }
    }

    class Recipes extends FabricRecipeProvider {
        public Recipes(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generate(Consumer<RecipeJsonProvider> exporter) {
            ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, USRegistry.PHYSICS_GUN_ITEM)
                    .criterion("get_item", InventoryChangedCriterion.Conditions.items(Items.NETHER_STAR))
                    .pattern("pia")
                    .pattern("p  ")
                    .input('a', Items.ENDER_EYE)
                    .input('p', ItemTags.PLANKS)
                    .input('i', Items.NETHER_STAR)
                    .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, USRegistry.TNT_CANNON_ITEM)
                    .criterion("get_item", InventoryChangedCriterion.Conditions.items(USRegistry.PHYSICS_GUN_ITEM))
                    .pattern("p ")
                    .pattern("i ")
                    .input('p', Items.STICK)
                    .input('i', USRegistry.PHYSICS_GUN_ITEM)
                    .offerTo(exporter);

            ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, USRegistry.BASEBALL_BAT_ITEM)
                    .criterion("get_item", InventoryChangedCriterion.Conditions.items(USRegistry.PHYSICS_GUN_ITEM))
                    .pattern(" p ")
                    .pattern("ipi")
                    .pattern(" i ")
                    .input('p', ItemTags.PLANKS)
                    .input('i', Items.STICK)
                    .offerTo(exporter);

            ShapelessRecipeJsonBuilder.create(RecipeCategory.REDSTONE, USRegistry.PHYSICAL_TNT_ITEM, 8)
                    .criterion("get_item", InventoryChangedCriterion.Conditions.items(Items.TNT))
                    .input(Items.ENDER_EYE)
                    .input(Items.TNT, 8)
                    .offerTo(exporter);

        }
    }
}
