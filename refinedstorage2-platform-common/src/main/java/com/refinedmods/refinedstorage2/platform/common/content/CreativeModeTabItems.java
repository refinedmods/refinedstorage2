package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.ItemStorageType;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void append(final Consumer<ItemStack> consumer) {
        appendBlocks(consumer);
        appendItems(consumer);
    }

    private static void appendBlocks(final Consumer<ItemStack> consumer) {
        final Consumer<ItemLike> itemConsumer = item -> consumer.accept(new ItemStack(item));
        Items.INSTANCE.getControllers().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getControllers().forEach(controllerItem -> consumer.accept(
            controllerItem.get().createAtEnergyCapacity()
        ));
        Items.INSTANCE.getCreativeControllers().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getCables().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getImporters().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getExporters().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getExternalStorages().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getConstructors().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getDestructors().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getWirelessTransmitters().stream().map(Supplier::get).forEach(itemConsumer);
        itemConsumer.accept(Blocks.INSTANCE.getDiskDrive());
        appendBlockColors(consumer, Blocks.INSTANCE.getGrid());
        appendBlockColors(consumer, Blocks.INSTANCE.getCraftingGrid());
        itemConsumer.accept(Items.INSTANCE.getPortableGrid());
        consumer.accept(Items.INSTANCE.getPortableGrid().createAtEnergyCapacity());
        itemConsumer.accept(Items.INSTANCE.getCreativePortableGrid());
        Items.INSTANCE.getDetectors().stream().map(Supplier::get).forEach(itemConsumer);
        itemConsumer.accept(Blocks.INSTANCE.getInterface());
        Arrays.stream(ItemStorageType.Variant.values()).forEach(variant -> itemConsumer.accept(
            Blocks.INSTANCE.getItemStorageBlock(variant)
        ));
        Arrays.stream(FluidStorageType.Variant.values()).forEach(variant -> itemConsumer.accept(
            Blocks.INSTANCE.getFluidStorageBlock(variant)
        ));
        itemConsumer.accept(Blocks.INSTANCE.getMachineCasing());
        itemConsumer.accept(Blocks.INSTANCE.getQuartzEnrichedIronBlock());
        itemConsumer.accept(Blocks.INSTANCE.getStorageMonitor());
        Items.INSTANCE.getNetworkTransmitters().stream().map(Supplier::get).forEach(itemConsumer);
        Items.INSTANCE.getNetworkReceivers().stream().map(Supplier::get).forEach(itemConsumer);
    }

    private static void appendBlockColors(final Consumer<ItemStack> consumer, final BlockColorMap<?, ?> map) {
        map.values().forEach(block -> consumer.accept(new ItemStack(block)));
    }

    private static void appendItems(final Consumer<ItemStack> consumer) {
        final Consumer<ItemLike> itemConsumer = item -> consumer.accept(new ItemStack(item));
        itemConsumer.accept(Items.INSTANCE.getQuartzEnrichedIron());
        itemConsumer.accept(Items.INSTANCE.getSilicon());
        itemConsumer.accept(Items.INSTANCE.getProcessorBinding());
        itemConsumer.accept(Items.INSTANCE.getWrench());

        Arrays.stream(ProcessorItem.Type.values()).map(Items.INSTANCE::getProcessor).forEach(itemConsumer);

        itemConsumer.accept(Items.INSTANCE.getConstructionCore());
        itemConsumer.accept(Items.INSTANCE.getDestructionCore());

        Arrays.stream(ItemStorageType.Variant.values())
            .filter(variant -> variant != ItemStorageType.Variant.CREATIVE)
            .map(Items.INSTANCE::getItemStoragePart)
            .forEach(itemConsumer);
        Arrays.stream(FluidStorageType.Variant.values())
            .filter(variant -> variant != FluidStorageType.Variant.CREATIVE)
            .map(Items.INSTANCE::getFluidStoragePart)
            .forEach(itemConsumer);

        Arrays.stream(ItemStorageType.Variant.values()).forEach(variant -> itemConsumer.accept(
            Items.INSTANCE.getItemStorageDisk(variant)
        ));
        Arrays.stream(FluidStorageType.Variant.values()).forEach(variant -> itemConsumer.accept(
            Items.INSTANCE.getFluidStorageDisk(variant)
        ));
        itemConsumer.accept(Items.INSTANCE.getStorageHousing());

        itemConsumer.accept(Items.INSTANCE.getUpgrade());
        itemConsumer.accept(Items.INSTANCE.getSpeedUpgrade());
        itemConsumer.accept(Items.INSTANCE.getStackUpgrade());
        itemConsumer.accept(Items.INSTANCE.getFortune1Upgrade());
        itemConsumer.accept(Items.INSTANCE.getFortune2Upgrade());
        itemConsumer.accept(Items.INSTANCE.getFortune3Upgrade());
        itemConsumer.accept(Items.INSTANCE.getSilkTouchUpgrade());
        itemConsumer.accept(Items.INSTANCE.getRegulatorUpgrade());
        itemConsumer.accept(Items.INSTANCE.getRangeUpgrade());
        itemConsumer.accept(Items.INSTANCE.getCreativeRangeUpgrade());
        itemConsumer.accept(Items.INSTANCE.getWirelessGrid());
        consumer.accept(Items.INSTANCE.getWirelessGrid().createAtEnergyCapacity());
        itemConsumer.accept(Items.INSTANCE.getCreativeWirelessGrid());
        itemConsumer.accept(Items.INSTANCE.getConfigurationCard());
        itemConsumer.accept(Items.INSTANCE.getNetworkCard());
    }
}
