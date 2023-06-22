package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorItem;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.world.level.ItemLike;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void append(final Consumer<ItemLike> consumer) {
        appendBlocks(consumer);
        appendItems(consumer);
    }

    private static void appendBlocks(final Consumer<ItemLike> consumer) {
        Items.INSTANCE.getAllControllers().stream().map(Supplier::get).forEach(consumer);
        Items.INSTANCE.getCables().stream().map(Supplier::get).forEach(consumer);
        Items.INSTANCE.getImporters().stream().map(Supplier::get).forEach(consumer);
        Items.INSTANCE.getExporters().stream().map(Supplier::get).forEach(consumer);
        Items.INSTANCE.getExternalStorages().stream().map(Supplier::get).forEach(consumer);
        Items.INSTANCE.getConstructors().stream().map(Supplier::get).forEach(consumer);
        Items.INSTANCE.getDestructors().stream().map(Supplier::get).forEach(consumer);
        consumer.accept(Blocks.INSTANCE.getDiskDrive());
        appendBlockColors(consumer, Blocks.INSTANCE.getGrid());
        appendBlockColors(consumer, Blocks.INSTANCE.getCraftingGrid());
        Items.INSTANCE.getDetectors().stream().map(Supplier::get).forEach(consumer);
        consumer.accept(Blocks.INSTANCE.getInterface());
        Arrays.stream(ItemStorageType.Variant.values()).forEach(variant -> consumer.accept(
            Blocks.INSTANCE.getItemStorageBlock(variant)
        ));
        Arrays.stream(FluidStorageType.Variant.values()).forEach(variant -> consumer.accept(
            Blocks.INSTANCE.getFluidStorageBlock(variant)
        ));
        consumer.accept(Blocks.INSTANCE.getMachineCasing());
        consumer.accept(Blocks.INSTANCE.getQuartzEnrichedIronBlock());
    }

    private static void appendBlockColors(final Consumer<ItemLike> consumer, final BlockColorMap<?> map) {
        map.values().forEach(consumer);
    }

    private static void appendItems(final Consumer<ItemLike> consumer) {
        consumer.accept(Items.INSTANCE.getQuartzEnrichedIron());
        consumer.accept(Items.INSTANCE.getSilicon());
        consumer.accept(Items.INSTANCE.getProcessorBinding());
        consumer.accept(Items.INSTANCE.getWrench());

        Arrays.stream(ProcessorItem.Type.values()).map(Items.INSTANCE::getProcessor).forEach(consumer);

        consumer.accept(Items.INSTANCE.getConstructionCore());
        consumer.accept(Items.INSTANCE.getDestructionCore());

        Arrays.stream(ItemStorageType.Variant.values())
            .filter(variant -> variant != ItemStorageType.Variant.CREATIVE)
            .map(Items.INSTANCE::getItemStoragePart)
            .forEach(consumer);
        Arrays.stream(FluidStorageType.Variant.values())
            .filter(variant -> variant != FluidStorageType.Variant.CREATIVE)
            .map(Items.INSTANCE::getFluidStoragePart)
            .forEach(consumer);

        Arrays.stream(ItemStorageType.Variant.values()).forEach(variant -> consumer.accept(
            Items.INSTANCE.getItemStorageDisk(variant)
        ));
        Arrays.stream(FluidStorageType.Variant.values()).forEach(variant -> consumer.accept(
            Items.INSTANCE.getFluidStorageDisk(variant)
        ));
        consumer.accept(Items.INSTANCE.getStorageHousing());

        consumer.accept(Items.INSTANCE.getUpgrade());
        consumer.accept(Items.INSTANCE.getSpeedUpgrade());
        consumer.accept(Items.INSTANCE.getStackUpgrade());
        consumer.accept(Items.INSTANCE.getFortune1Upgrade());
        consumer.accept(Items.INSTANCE.getFortune2Upgrade());
        consumer.accept(Items.INSTANCE.getFortune3Upgrade());
        consumer.accept(Items.INSTANCE.getSilkTouchUpgrade());
    }
}
