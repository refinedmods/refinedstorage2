package com.refinedmods.refinedstorage.neoforge.datagen.recipe;

import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.StorageContainerUpgradeRecipe;
import com.refinedmods.refinedstorage.common.storage.StorageVariant;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeWithEnchantedBookRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class MainRecipeProvider extends RecipeProvider {
    private static final TagKey<Item> SILICON = TagKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath("c", "silicon"));

    public MainRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        constructionCore();
        destructionCore();
        autocraftingMonitor();
        storageDisksAndBlocks();
        storageParts();
        fluidStorageParts();
        storageUpgrades();
        machineCasing();
        quartzEnrichedIron();
        quartzEnrichedCopper();
        processorBinding();
        rawProcessors();
        wrench();
        cable();
        controller();
        diskDrive();
        grid();
        craftingGrid();
        patternGrid();
        pattern();
        upgrade();
        speedUpgrade();
        stackUpgrade();
        regulatorUpgrade();
        rangeUpgrade();
        autocraftingUpgrade();
        networkCard();
        configurationCard();
        securityCard();
        fallbackSecurityCard();
        securityManager();
        storageHousing();
        storageMonitor();
        portableGrid();
        exporter();
        importer();
        networkInterface();
        externalStorage();
        destructor();
        constructor();
        detector();
        relay();
        diskInterface();
        autocrafter();
        autocrafterManager();
        networkReceiver();
        networkTransmitter();
        wirelessTransmitter();
        wirelessGrid();
        wirelessAutocraftingMonitor();
        silicon();
        enchantedBookUpgrades();
    }

    protected void constructionCore() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Items.INSTANCE.getConstructionCore())
            .requires(Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC))
            .requires(Tags.Items.DUSTS_GLOWSTONE)
            .unlockedBy("has_processor", has(Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC)))
            .save(output);
    }

    protected void destructionCore() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Items.INSTANCE.getDestructionCore())
            .requires(Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC))
            .requires(Tags.Items.GEMS_QUARTZ)
            .unlockedBy("has_processor", has(Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC)))
            .save(output);
    }

    private void autocraftingMonitor() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getAutocraftingMonitor().getDefault())
            .pattern("PAG")
            .pattern("EMG")
            .pattern("PAG")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .define('A', Items.INSTANCE.getPattern())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .unlockedBy("has_processor", has(Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED)))
            .save(output);
    }

    private void storageDisksAndBlocks() {
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            if (variant == ItemStorageVariant.CREATIVE) {
                continue;
            }
            ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getItemStorageDisk(variant))
                .pattern("GRG")
                .pattern("RPR")
                .pattern("EEE")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('P', Items.INSTANCE.getItemStoragePart(variant))
                .define('E', Items.INSTANCE.getQuartzEnrichedIron())
                .unlockedBy("has_storage_part", has(Items.INSTANCE.getItemStoragePart(variant)))
                .save(output);
            ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getItemStorageBlock(variant))
                .pattern("EPE")
                .pattern("EME")
                .pattern("ERE")
                .define('M', Blocks.INSTANCE.getMachineCasing())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('P', Items.INSTANCE.getItemStoragePart(variant))
                .define('E', Items.INSTANCE.getQuartzEnrichedIron())
                .unlockedBy("has_storage_part", has(Items.INSTANCE.getItemStoragePart(variant)))
                .save(output);
            ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Items.INSTANCE.getItemStorageDisk(variant))
                .requires(Items.INSTANCE.getStorageHousing())
                .requires(Items.INSTANCE.getItemStoragePart(variant))
                .unlockedBy("has_storage_part", has(Items.INSTANCE.getItemStoragePart(variant)))
                .save(output, MOD_ID + ":" + variant.getName() + "_storage_disk_from_storage_housing");
        }

        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            if (variant == FluidStorageVariant.CREATIVE) {
                continue;
            }
            ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getFluidStorageDisk(variant))
                .pattern("GRG")
                .pattern("RPR")
                .pattern("EEE")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('P', Items.INSTANCE.getFluidStoragePart(variant))
                .define('E', Items.INSTANCE.getQuartzEnrichedIron())
                .unlockedBy("has_storage_part", has(Items.INSTANCE.getFluidStoragePart(variant)))
                .save(output);
            ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getFluidStorageBlock(variant))
                .pattern("EPE")
                .pattern("EME")
                .pattern("ERE")
                .define('M', Blocks.INSTANCE.getMachineCasing())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('P', Items.INSTANCE.getFluidStoragePart(variant))
                .define('E', Items.INSTANCE.getQuartzEnrichedIron())
                .unlockedBy("has_storage_part", has(Items.INSTANCE.getFluidStoragePart(variant)))
                .save(output);
            ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Items.INSTANCE.getFluidStorageDisk(variant))
                .requires(Items.INSTANCE.getStorageHousing())
                .requires(Items.INSTANCE.getFluidStoragePart(variant))
                .unlockedBy("has_storage_part", has(Items.INSTANCE.getFluidStoragePart(variant)))
                .save(output, MOD_ID + ":" + variant.getName() + "_fluid_storage_disk_from_storage_housing");
        }
    }

    private void storageParts() {
        // 1k
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getItemStoragePart(ItemStorageVariant.ONE_K))
            .pattern("SES")
            .pattern("GRG")
            .pattern("SGS")
            .define('S', SILICON)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
        // 4k
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getItemStoragePart(ItemStorageVariant.FOUR_K))
            .pattern("PEP")
            .pattern("SRS")
            .pattern("PSP")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC))
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Items.INSTANCE.getItemStoragePart(ItemStorageVariant.ONE_K))
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_1k_storage_part", has(Items.INSTANCE.getItemStoragePart(ItemStorageVariant.ONE_K)))
            .save(output);
        // 16k
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getItemStoragePart(ItemStorageVariant.SIXTEEN_K))
            .pattern("PEP")
            .pattern("SRS")
            .pattern("PSP")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Items.INSTANCE.getItemStoragePart(ItemStorageVariant.FOUR_K))
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_4k_storage_part", has(Items.INSTANCE.getItemStoragePart(ItemStorageVariant.FOUR_K)))
            .save(output);
        // 64k
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getItemStoragePart(ItemStorageVariant.SIXTY_FOUR_K))
            .pattern("PEP")
            .pattern("SRS")
            .pattern("PSP")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Items.INSTANCE.getItemStoragePart(ItemStorageVariant.SIXTEEN_K))
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_16k_storage_part", has(Items.INSTANCE.getItemStoragePart(ItemStorageVariant.SIXTEEN_K)))
            .save(output);
    }

    private void fluidStorageParts() {
        // 64b
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.SIXTY_FOUR_B))
            .pattern("SES")
            .pattern("GRG")
            .pattern("SGS")
            .define('S', SILICON)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('R', Tags.Items.BUCKETS_EMPTY)
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
        // 256b
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.TWO_HUNDRED_FIFTY_SIX_B))
            .pattern("PEP")
            .pattern("SRS")
            .pattern("PSP")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC))
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.SIXTY_FOUR_B))
            .define('R', Tags.Items.BUCKETS_EMPTY)
            .unlockedBy("has_64b_fluid_storage_part",
                has(Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.SIXTY_FOUR_B)))
            .save(output);
        // 1024b
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.THOUSAND_TWENTY_FOUR_B))
            .pattern("PEP")
            .pattern("SRS")
            .pattern("PSP")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.TWO_HUNDRED_FIFTY_SIX_B))
            .define('R', Tags.Items.BUCKETS_EMPTY)
            .unlockedBy("has_256b_fluid_storage_part",
                has(Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.TWO_HUNDRED_FIFTY_SIX_B)))
            .save(output);
        // 4096b
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC,
                Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.FOUR_THOUSAND_NINETY_SIX_B))
            .pattern("PEP")
            .pattern("SRS")
            .pattern("PSP")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.THOUSAND_TWENTY_FOUR_B))
            .define('R', Tags.Items.BUCKETS_EMPTY)
            .unlockedBy("has_1024b_fluid_storage_part",
                has(Items.INSTANCE.getFluidStoragePart(FluidStorageVariant.THOUSAND_TWENTY_FOUR_B)))
            .save(output);
    }

    private void storageUpgrades() {
        storageUpgrades(ItemStorageVariant.values(), Items.INSTANCE::getItemStoragePart,
            Items.INSTANCE::getItemStorageDisk, "storage_disk_upgrade");
        storageUpgrades(FluidStorageVariant.values(), Items.INSTANCE::getFluidStoragePart,
            Items.INSTANCE::getFluidStorageDisk, "fluid_storage_disk_upgrade");
        storageUpgrades(ItemStorageVariant.values(), Items.INSTANCE::getItemStoragePart,
            Blocks.INSTANCE::getItemStorageBlock, "storage_block_upgrade");
        storageUpgrades(FluidStorageVariant.values(), Items.INSTANCE::getFluidStoragePart,
            Blocks.INSTANCE::getFluidStorageBlock, "fluid_storage_block_upgrade");
    }

    @SuppressWarnings("deprecation")
    private <T extends StorageVariant> void storageUpgrades(final T[] variants,
                                                            final Function<T, ItemLike> partProvider,
                                                            final Function<T, ItemLike> containerProvider,
                                                            final String name) {
        for (final T variant : variants) {
            if (variant.getCapacity() == null) {
                continue;
            }
            final List<T> lowerVariants = Arrays.stream(variants)
                .filter(otherVariant -> otherVariant.getCapacity() != null)
                .filter(otherVariant -> otherVariant.getCapacity() < variant.getCapacity())
                .toList();
            if (lowerVariants.isEmpty()) {
                continue;
            }
            final ItemLike part = partProvider.apply(variant);
            final Identifier recipeId = createIdentifier(variant.getName() + "_" + name);
            output.accept(ResourceKey.create(Registries.RECIPE, recipeId), new StorageContainerUpgradeRecipe(
                lowerVariants.stream()
                    .map(containerProvider)
                    .map(ItemLike::asItem)
                    .map(Item::builtInRegistryHolder)
                    .map(holder -> registries.holderOrThrow(holder.key()))
                    .toList(),
                registries.holderOrThrow(part.asItem().builtInRegistryHolder().key()),
                registries.holderOrThrow(containerProvider.apply(variant).asItem().builtInRegistryHolder().key())
            ), null);
        }
    }

    private void machineCasing() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getMachineCasing())
            .pattern("EEE")
            .pattern("ESE")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Tags.Items.STONES)
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
    }

    private void quartzEnrichedIron() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getQuartzEnrichedIron(), 4)
            .pattern("II")
            .pattern("IQ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('Q', Tags.Items.GEMS_QUARTZ)
            .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ))
            .save(output);
    }

    private void quartzEnrichedCopper() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getQuartzEnrichedCopper(), 4)
            .pattern("CC")
            .pattern("CQ")
            .define('C', Tags.Items.INGOTS_COPPER)
            .define('Q', Tags.Items.GEMS_QUARTZ)
            .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ))
            .save(output);
    }

    private void processorBinding() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getProcessorBinding(), 8)
            .pattern("SLS")
            .define('S', Tags.Items.STRINGS)
            .define('L', Tags.Items.SLIME_BALLS)
            .unlockedBy("has_slime_ball", has(Tags.Items.SLIME_BALLS))
            .save(output);
    }

    private void rawProcessors() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC,
                Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_BASIC))
            .requires(Items.INSTANCE.getProcessorBinding())
            .requires(Tags.Items.INGOTS_IRON)
            .requires(SILICON)
            .requires(Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_processor_binding", has(Items.INSTANCE.getProcessorBinding()))
            .save(output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC,
                Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_IMPROVED))
            .requires(Items.INSTANCE.getProcessorBinding())
            .requires(Tags.Items.INGOTS_GOLD)
            .requires(SILICON)
            .requires(Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_processor_binding", has(Items.INSTANCE.getProcessorBinding()))
            .save(output);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC,
                Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_ADVANCED))
            .requires(Items.INSTANCE.getProcessorBinding())
            .requires(Tags.Items.GEMS_DIAMOND)
            .requires(SILICON)
            .requires(Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_processor_binding", has(Items.INSTANCE.getProcessorBinding()))
            .save(output);
        SimpleCookingRecipeBuilder.smelting(
                Ingredient.of(Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_BASIC)),
                RecipeCategory.MISC,
                CookingBookCategory.MISC,
                Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC),
                0.5F,
                200
            )
            .unlockedBy("has_raw_basic_processor", has(Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_BASIC)))
            .save(output);
        SimpleCookingRecipeBuilder.smelting(
                Ingredient.of(Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_IMPROVED)),
                RecipeCategory.MISC,
                CookingBookCategory.MISC,
                Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED),
                0.5F,
                200
            )
            .unlockedBy("has_raw_improved_processor", has(Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_IMPROVED)))
            .save(output);
        SimpleCookingRecipeBuilder.smelting(
                Ingredient.of(Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_ADVANCED)),
                RecipeCategory.MISC,
                CookingBookCategory.MISC,
                Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED),
                0.5F,
                200
            )
            .unlockedBy("has_raw_advanced_processor", has(Items.INSTANCE.getProcessor(ProcessorItem.Type.RAW_ADVANCED)))
            .save(output);
    }

    private void wrench() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getWrench())
            .pattern("EPE")
            .pattern("EEE")
            .pattern(" E ")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC))
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
    }

    private void cable() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getCable().getDefault(), 12)
            .pattern("EEE")
            .pattern("GRG")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedCopper())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_quartz_enriched_copper", has(Items.INSTANCE.getQuartzEnrichedCopper()))
            .save(output);
    }

    private void controller() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getController().getDefault())
            .pattern("EPE")
            .pattern("SMS")
            .pattern("ESE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('S', SILICON)
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void diskDrive() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getDiskDrive())
            .pattern("ECE")
            .pattern("EME")
            .pattern("EPE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', Tags.Items.CHESTS)
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void grid() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getGrid().getDefault())
            .pattern("PCG")
            .pattern("EMG")
            .pattern("PDG")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('D', Items.INSTANCE.getDestructionCore())
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void craftingGrid() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Blocks.INSTANCE.getCraftingGrid().getDefault())
            .requires(Blocks.INSTANCE.getGrid().getDefault())
            .requires(Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .requires(Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
            .unlockedBy("has_grid", has(Blocks.INSTANCE.getGrid().getDefault()))
            .save(output);
    }

    private void patternGrid() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Blocks.INSTANCE.getPatternGrid().getDefault())
            .requires(Blocks.INSTANCE.getGrid().getDefault())
            .requires(Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .requires(Items.INSTANCE.getPattern())
            .unlockedBy("has_grid", has(Blocks.INSTANCE.getGrid().getDefault()))
            .save(output);
    }

    private void pattern() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getPattern())
            .pattern("GRG")
            .pattern("RGR")
            .pattern("EEE")
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
    }

    private void upgrade() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getUpgrade())
            .pattern("EGE")
            .pattern("EPE")
            .pattern("EGE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
    }

    private void speedUpgrade() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getSpeedUpgrade())
            .pattern("ESE")
            .pattern("SUS")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', net.minecraft.world.item.Items.SUGAR)
            .define('U', Items.INSTANCE.getUpgrade())
            .unlockedBy("has_upgrade", has(Items.INSTANCE.getUpgrade()))
            .save(output);
    }

    private void stackUpgrade() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getStackUpgrade())
            .pattern("USU")
            .pattern("SUS")
            .pattern("USU")
            .define('U', net.minecraft.world.item.Items.SUGAR)
            .define('S', Items.INSTANCE.getSpeedUpgrade())
            .unlockedBy("has_speed_upgrade", has(Items.INSTANCE.getSpeedUpgrade()))
            .save(output);
    }

    private void regulatorUpgrade() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getRegulatorUpgrade())
            .pattern("ECE")
            .pattern("RUR")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('C', net.minecraft.world.item.Items.COMPARATOR)
            .define('U', Items.INSTANCE.getUpgrade())
            .unlockedBy("has_upgrade", has(Items.INSTANCE.getUpgrade()))
            .save(output);
    }

    private void rangeUpgrade() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getRangeUpgrade())
            .pattern("EPE")
            .pattern("PUP")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('P', Tags.Items.ENDER_PEARLS)
            .define('U', Items.INSTANCE.getUpgrade())
            .unlockedBy("has_upgrade", has(Items.INSTANCE.getUpgrade()))
            .save(output);
    }

    private void autocraftingUpgrade() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getAutocraftingUpgrade())
            .pattern("EOE")
            .pattern("CUC")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
            .define('O', Items.INSTANCE.getConstructionCore())
            .define('U', Items.INSTANCE.getUpgrade())
            .unlockedBy("has_upgrade", has(Items.INSTANCE.getUpgrade()))
            .save(output);
    }

    private void networkCard() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getNetworkCard())
            .pattern("EEE")
            .pattern("PAP")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('P', net.minecraft.world.item.Items.PAPER)
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
    }

    private void configurationCard() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getConfigurationCard())
            .pattern("EEE")
            .pattern("PAP")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('P', net.minecraft.world.item.Items.PAPER)
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
    }

    private void securityCard() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getSecurityCard())
            .pattern("EEE")
            .pattern("CAC")
            .pattern("EEE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', Items.INSTANCE.getNetworkCard())
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .unlockedBy("has_network_card", has(Items.INSTANCE.getNetworkCard()))
            .save(output);
    }

    private void fallbackSecurityCard() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Items.INSTANCE.getFallbackSecurityCard())
            .requires(Items.INSTANCE.getSecurityCard())
            .requires(net.minecraft.world.item.Items.PAPER)
            .unlockedBy("has_security_card", has(Items.INSTANCE.getSecurityCard()))
            .save(output);
    }

    private void securityManager() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getSecurityManager().getDefault())
            .pattern("ECE")
            .pattern("SMS")
            .pattern("EFE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', Tags.Items.CHESTS)
            .define('S', Items.INSTANCE.getSecurityCard())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('F', Items.INSTANCE.getFallbackSecurityCard())
            .unlockedBy("has_security_card", has(Items.INSTANCE.getSecurityCard()))
            .save(output);
    }

    private void storageHousing() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getStorageHousing())
            .pattern("GRG")
            .pattern("R R")
            .pattern("EEE")
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .unlockedBy("has_quartz_enriched_iron", has(Items.INSTANCE.getQuartzEnrichedIron()))
            .save(output);
    }

    private void storageMonitor() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getStorageMonitor())
            .pattern("PCG")
            .pattern("EMG")
            .pattern("PDG")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.BASIC))
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('D', Items.INSTANCE.getDestructionCore())
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void portableGrid() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getPortableGrid())
            .pattern("EGE")
            .pattern("ECE")
            .pattern("EGE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('G', com.refinedmods.refinedstorage.common.content.Tags.GRIDS)
            .define('C', com.refinedmods.refinedstorage.common.content.Tags.CONTROLLERS)
            .unlockedBy("has_grid", has(com.refinedmods.refinedstorage.common.content.Tags.GRIDS))
            .save(output);
    }

    private void exporter() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Blocks.INSTANCE.getExporter().getDefault())
            .requires(Blocks.INSTANCE.getCable().getDefault())
            .requires(Items.INSTANCE.getConstructionCore())
            .requires(Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_construction_core", has(Items.INSTANCE.getConstructionCore()))
            .save(output);
    }

    private void importer() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Blocks.INSTANCE.getImporter().getDefault())
            .requires(Blocks.INSTANCE.getCable().getDefault())
            .requires(Items.INSTANCE.getDestructionCore())
            .requires(Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_destruction_core", has(Items.INSTANCE.getDestructionCore()))
            .save(output);
    }

    private void networkInterface() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getInterface())
            .pattern("UIU")
            .pattern("RMR")
            .pattern("UEU")
            .define('I', Blocks.INSTANCE.getImporter().getDefault())
            .define('E', Blocks.INSTANCE.getExporter().getDefault())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('U', Items.INSTANCE.getQuartzEnrichedIron())
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void externalStorage() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getExternalStorage().getDefault())
            .pattern("CED")
            .pattern("HMH")
            .pattern("EPE")
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('D', Items.INSTANCE.getDestructionCore())
            .define('H', Tags.Items.CHESTS)
            .define('M', Blocks.INSTANCE.getCable().getDefault())
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_construction_core", has(Items.INSTANCE.getConstructionCore()))
            .save(output);
    }

    private void destructor() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getDestructor().getDefault())
            .pattern("EDE")
            .pattern("ICI")
            .pattern("EPE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('D', Items.INSTANCE.getDestructionCore())
            .define('I', Tags.Items.GEMS_DIAMOND)
            .define('C', Blocks.INSTANCE.getCable().getDefault())
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_destruction_core", has(Items.INSTANCE.getDestructionCore()))
            .save(output);
    }

    private void constructor() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getConstructor().getDefault())
            .pattern("ECE")
            .pattern("RMR")
            .pattern("EIE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('R', Tags.Items.GEMS_DIAMOND)
            .define('M', Blocks.INSTANCE.getCable().getDefault())
            .define('I', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_construction_core", has(Items.INSTANCE.getConstructionCore()))
            .save(output);
    }

    private void detector() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getDetector().getDefault())
            .pattern("ERE")
            .pattern("CMC")
            .pattern("EPE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', net.minecraft.world.item.Items.COMPARATOR)
            .define('R', net.minecraft.world.item.Items.REDSTONE_TORCH)
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.IMPROVED))
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void relay() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Blocks.INSTANCE.getRelay().getDefault())
            .requires(Blocks.INSTANCE.getMachineCasing())
            .requires(Blocks.INSTANCE.getCable().getDefault())
            .requires(Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .requires(net.minecraft.world.item.Items.REDSTONE_TORCH)
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void diskInterface() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getDiskInterface().getDefault())
            .pattern("ESE")
            .pattern("CMD")
            .pattern("ESE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('S', Items.INSTANCE.getStorageHousing())
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('D', Items.INSTANCE.getDestructionCore())
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void autocrafter() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getAutocrafter().getDefault())
            .pattern("ECE")
            .pattern("AMA")
            .pattern("EDE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('D', Items.INSTANCE.getDestructionCore())
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void autocrafterManager() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getAutocrafterManager().getDefault())
            .pattern("PCG")
            .pattern("EMG")
            .pattern("PCG")
            .define('P', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('C', com.refinedmods.refinedstorage.common.content.Tags.AUTOCRAFTERS)
            .define('G', Tags.Items.GLASS_BLOCKS)
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .unlockedBy("has_autocrafter", has(com.refinedmods.refinedstorage.common.content.Tags.AUTOCRAFTERS))
            .save(output);
    }

    private void networkReceiver() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getNetworkReceiver().getDefault())
            .pattern("ANA")
            .pattern("CMD")
            .pattern("EEE")
            .define('E', Tags.Items.ENDER_PEARLS)
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('D', Items.INSTANCE.getDestructionCore())
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('N', Tags.Items.INGOTS_NETHERITE)
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void networkTransmitter() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getNetworkTransmitter().getDefault())
            .pattern("EEE")
            .pattern("CMD")
            .pattern("ANA")
            .define('E', Tags.Items.ENDER_PEARLS)
            .define('C', Items.INSTANCE.getConstructionCore())
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('D', Items.INSTANCE.getDestructionCore())
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('N', Tags.Items.INGOTS_NETHERITE)
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void wirelessTransmitter() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Blocks.INSTANCE.getWirelessTransmitter().getDefault())
            .pattern("EPE")
            .pattern("EME")
            .pattern("EAE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .define('M', Blocks.INSTANCE.getMachineCasing())
            .define('P', Tags.Items.ENDER_PEARLS)
            .unlockedBy("has_machine_casing", has(Blocks.INSTANCE.getMachineCasing()))
            .save(output);
    }

    private void wirelessGrid() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getWirelessGrid())
            .pattern("EPE")
            .pattern("EGE")
            .pattern("EAE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('P', Tags.Items.ENDER_PEARLS)
            .define('G', com.refinedmods.refinedstorage.common.content.Tags.GRIDS)
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .unlockedBy("has_grid", has(com.refinedmods.refinedstorage.common.content.Tags.GRIDS))
            .save(output);
    }

    private void wirelessAutocraftingMonitor() {
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, Items.INSTANCE.getWirelessAutocraftingMonitor())
            .pattern("EPE")
            .pattern("EME")
            .pattern("EAE")
            .define('E', Items.INSTANCE.getQuartzEnrichedIron())
            .define('P', Tags.Items.ENDER_PEARLS)
            .define('M', com.refinedmods.refinedstorage.common.content.Tags.AUTOCRAFTING_MONITORS)
            .define('A', Items.INSTANCE.getProcessor(ProcessorItem.Type.ADVANCED))
            .unlockedBy("has_autocrafting_monitor",
                has(com.refinedmods.refinedstorage.common.content.Tags.AUTOCRAFTING_MONITORS))
            .save(output);
    }

    private void silicon() {
        SimpleCookingRecipeBuilder.smelting(
                tag(Tags.Items.GEMS_QUARTZ),
                RecipeCategory.MISC,
                CookingBookCategory.MISC,
                Items.INSTANCE.getSilicon(),
                0.5F,
                200)
            .unlockedBy("has_quartz", has(Tags.Items.GEMS_QUARTZ))
            .save(output);
    }

    private void enchantedBookUpgrades() {
        output.accept(ResourceKey.create(Registries.RECIPE, ContentIds.FORTUNE_1_UPGRADE),
            new UpgradeWithEnchantedBookRecipe(this.registries.holderOrThrow(Enchantments.FORTUNE), 1,
                new ItemStackTemplate(Items.INSTANCE.getFortune1Upgrade())), null);
        output.accept(ResourceKey.create(Registries.RECIPE, ContentIds.FORTUNE_2_UPGRADE),
            new UpgradeWithEnchantedBookRecipe(this.registries.holderOrThrow(Enchantments.FORTUNE), 2,
                new ItemStackTemplate(Items.INSTANCE.getFortune2Upgrade())), null);
        output.accept(ResourceKey.create(Registries.RECIPE, ContentIds.FORTUNE_3_UPGRADE),
            new UpgradeWithEnchantedBookRecipe(this.registries.holderOrThrow(Enchantments.FORTUNE), 3,
                new ItemStackTemplate(Items.INSTANCE.getFortune3Upgrade())), null);
        output.accept(ResourceKey.create(Registries.RECIPE, ContentIds.SILK_TOUCH_UPGRADE),
            new UpgradeWithEnchantedBookRecipe(this.registries.holderOrThrow(Enchantments.SILK_TOUCH), 0,
                new ItemStackTemplate(Items.INSTANCE.getSilkTouchUpgrade())), null);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(final HolderLookup.Provider registries,
                                                      final RecipeOutput output) {
            return new MainRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Refined Storage recipes";
        }
    }
}
