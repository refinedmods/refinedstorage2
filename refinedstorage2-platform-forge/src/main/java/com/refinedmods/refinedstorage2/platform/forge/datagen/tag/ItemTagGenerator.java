package com.refinedmods.refinedstorage2.platform.forge.datagen.tag;

import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.ItemStorageType;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage2.platform.common.content.Tags.CABLES;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.CONSTRUCTORS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.CONTROLLERS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.CRAFTING_GRIDS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.CREATIVE_CONTROLLERS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.DESTRUCTORS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.DETECTORS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.EXPORTERS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.EXTERNAL_STORAGES;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.FLUID_STORAGE_DISKS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.GRIDS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.IMPORTERS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.NETWORK_RECEIVERS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.STORAGE_DISKS;
import static com.refinedmods.refinedstorage2.platform.common.content.Tags.WIRELESS_TRANSMITTERS;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(final PackOutput packOutput,
                            final CompletableFuture<HolderLookup.Provider> registries,
                            final TagsProvider<Block> blockTagsProvider,
                            final ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, blockTagsProvider.contentsGetter(), MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        addAllToTag(CABLES, Items.INSTANCE.getCables());
        addAllToTag(CONTROLLERS, Items.INSTANCE.getControllers());
        addAllToTag(CREATIVE_CONTROLLERS, Blocks.INSTANCE.getCreativeController().values().stream()
            .map(Block::asItem)
            .map(c -> (Supplier<Item>) () -> c)
            .toList());
        addAllToTag(FLUID_STORAGE_DISKS,
            Arrays.stream(FluidStorageType.Variant.values())
                .filter(variant -> variant != FluidStorageType.Variant.CREATIVE)
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
        addAllToTag(STORAGE_DISKS,
            Arrays.stream(ItemStorageType.Variant.values())
                .filter(variant -> variant != ItemStorageType.Variant.CREATIVE)
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
    }

    private <T extends Item> void addAllToTag(final TagKey<Item> t, final Collection<Supplier<T>> items) {
        tag(t)
            .add(items.stream().map(Supplier::get).toArray(Item[]::new))
            .replace(false);
    }
}
