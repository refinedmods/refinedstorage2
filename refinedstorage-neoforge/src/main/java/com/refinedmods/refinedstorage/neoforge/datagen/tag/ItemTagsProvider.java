package com.refinedmods.refinedstorage.neoforge.datagen.tag;

import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;

import static com.refinedmods.refinedstorage.common.content.Tags.AUTOCRAFTERS;
import static com.refinedmods.refinedstorage.common.content.Tags.AUTOCRAFTER_MANAGERS;
import static com.refinedmods.refinedstorage.common.content.Tags.AUTOCRAFTING_MONITORS;
import static com.refinedmods.refinedstorage.common.content.Tags.CABLES;
import static com.refinedmods.refinedstorage.common.content.Tags.CONSTRUCTORS;
import static com.refinedmods.refinedstorage.common.content.Tags.CONTROLLERS;
import static com.refinedmods.refinedstorage.common.content.Tags.CRAFTING_GRIDS;
import static com.refinedmods.refinedstorage.common.content.Tags.CREATIVE_CONTROLLERS;
import static com.refinedmods.refinedstorage.common.content.Tags.DESTRUCTORS;
import static com.refinedmods.refinedstorage.common.content.Tags.DETECTORS;
import static com.refinedmods.refinedstorage.common.content.Tags.DISK_INTERFACES;
import static com.refinedmods.refinedstorage.common.content.Tags.EXPORTERS;
import static com.refinedmods.refinedstorage.common.content.Tags.EXTERNAL_STORAGES;
import static com.refinedmods.refinedstorage.common.content.Tags.FLUID_STORAGE_DISKS;
import static com.refinedmods.refinedstorage.common.content.Tags.GRIDS;
import static com.refinedmods.refinedstorage.common.content.Tags.IMPORTERS;
import static com.refinedmods.refinedstorage.common.content.Tags.NETWORK_RECEIVERS;
import static com.refinedmods.refinedstorage.common.content.Tags.NETWORK_TRANSMITTERS;
import static com.refinedmods.refinedstorage.common.content.Tags.PATTERN_GRIDS;
import static com.refinedmods.refinedstorage.common.content.Tags.RELAYS;
import static com.refinedmods.refinedstorage.common.content.Tags.SECURITY_MANAGERS;
import static com.refinedmods.refinedstorage.common.content.Tags.STORAGE_DISKS;
import static com.refinedmods.refinedstorage.common.content.Tags.WIRELESS_TRANSMITTERS;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;

public class ItemTagsProvider extends BlockTagCopyingItemTagProvider {
    private static final TagKey<Item> WRENCH = TagKey.create(
        Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/wrench"));
    private static final TagKey<Item> INGOTS = TagKey.create(
        Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots"));
    private static final TagKey<Item> SILICON = TagKey.create(
        Registries.ITEM, Identifier.fromNamespaceAndPath("c", "silicon"));

    public ItemTagsProvider(final PackOutput packOutput,
                            final CompletableFuture<HolderLookup.Provider> registries,
                            final CompletableFuture<TagLookup<Block>> blockTagsProvider) {
        super(packOutput, registries, blockTagsProvider, MOD_ID);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        addAllToTag2(CABLES, Items.INSTANCE.getCables());
        addAllToTag(CONTROLLERS, Items.INSTANCE.getControllers());
        addAllToTag(CREATIVE_CONTROLLERS, Blocks.INSTANCE.getCreativeController().values().stream()
            .map(Block::asItem)
            .map(c -> (Supplier<Item>) () -> c)
            .toList());
        addAllToTag(FLUID_STORAGE_DISKS,
            Arrays.stream(FluidStorageVariant.values())
                .filter(variant -> variant != FluidStorageVariant.CREATIVE)
                .map(Items.INSTANCE::getFluidStorageDisk)
                .map(t -> (Supplier<Item>) () -> t)
                .toList());
        addAllToTag(GRIDS,
            Blocks.INSTANCE.getGrid().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(CRAFTING_GRIDS,
            Blocks.INSTANCE.getCraftingGrid().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(PATTERN_GRIDS,
            Blocks.INSTANCE.getPatternGrid().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(STORAGE_DISKS,
            Arrays.stream(ItemStorageVariant.values())
                .filter(variant -> variant != ItemStorageVariant.CREATIVE)
                .map(Items.INSTANCE::getItemStorageDisk)
                .map(t -> (Supplier<Item>) () -> t)
                .toList());
        addAllToTag(IMPORTERS,
            Blocks.INSTANCE.getImporter().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(EXPORTERS,
            Blocks.INSTANCE.getExporter().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(EXTERNAL_STORAGES,
            Blocks.INSTANCE.getExternalStorage().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(DETECTORS,
            Blocks.INSTANCE.getDetector().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(CONSTRUCTORS,
            Blocks.INSTANCE.getConstructor().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(DESTRUCTORS,
            Blocks.INSTANCE.getDestructor().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(WIRELESS_TRANSMITTERS,
            Blocks.INSTANCE.getWirelessTransmitter().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(NETWORK_RECEIVERS,
            Blocks.INSTANCE.getNetworkReceiver().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(NETWORK_TRANSMITTERS,
            Blocks.INSTANCE.getNetworkTransmitter().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(SECURITY_MANAGERS,
            Blocks.INSTANCE.getSecurityManager().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(RELAYS,
            Blocks.INSTANCE.getRelay().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(DISK_INTERFACES,
            Blocks.INSTANCE.getDiskInterface().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(AUTOCRAFTERS,
            Blocks.INSTANCE.getAutocrafter().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(AUTOCRAFTER_MANAGERS,
            Blocks.INSTANCE.getAutocrafterManager().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        addAllToTag(AUTOCRAFTING_MONITORS,
            Blocks.INSTANCE.getAutocraftingMonitor().values().stream()
                .map(block -> (Supplier<Item>) block::asItem)
                .toList());
        tag(WRENCH).add(Items.INSTANCE.getWrench()).replace(false);
        tag(SILICON).add(Items.INSTANCE.getSilicon()).replace(false);
        tag(INGOTS)
            .add(Items.INSTANCE.getQuartzEnrichedIron())
            .add(Items.INSTANCE.getQuartzEnrichedCopper())
            .replace(false);
    }

    private <T extends Item> void addAllToTag(final TagKey<Item> t, final Collection<Supplier<T>> items) {
        tag(t).add(items.stream().map(Supplier::get).toArray(Item[]::new)).replace(false);
    }

    private void addAllToTag2(final TagKey<Item> t, final Collection<Supplier<BaseBlockItem>> items) {
        tag(t).add(items.stream().map(Supplier::get).toArray(Item[]::new)).replace(false);
    }
}
