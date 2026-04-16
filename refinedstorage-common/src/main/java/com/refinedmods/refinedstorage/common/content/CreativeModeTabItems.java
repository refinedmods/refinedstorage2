package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import java.util.Arrays;
import java.util.function.Consumer;

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
        final boolean requireEnergy = Platform.INSTANCE.getConfig().isRequireEnergy();
        final Consumer<ItemLike> itemConsumer = item -> consumer.accept(new ItemStack(item));
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getController());
        if (requireEnergy) {
            consumer.accept(Items.INSTANCE.getControllers().getFirst().get().createAtEnergyCapacity());
            appendDefaultBlockColor(consumer, Blocks.INSTANCE.getCreativeController());
        }
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getCable());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getImporter());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getExporter());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getExternalStorage());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getConstructor());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getDestructor());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getWirelessTransmitter());
        itemConsumer.accept(Blocks.INSTANCE.getDiskDrive());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getGrid());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getCraftingGrid());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getPatternGrid());
        itemConsumer.accept(Items.INSTANCE.getPortableGrid());
        if (requireEnergy) {
            consumer.accept(Items.INSTANCE.getPortableGrid().createAtEnergyCapacity());
            itemConsumer.accept(Items.INSTANCE.getCreativePortableGrid());
        }
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getDetector());
        itemConsumer.accept(Blocks.INSTANCE.getInterface());
        Arrays.stream(ItemStorageVariant.values()).forEach(variant -> itemConsumer.accept(
            Blocks.INSTANCE.getItemStorageBlock(variant)
        ));
        Arrays.stream(FluidStorageVariant.values()).forEach(variant -> itemConsumer.accept(
            Blocks.INSTANCE.getFluidStorageBlock(variant)
        ));
        itemConsumer.accept(Blocks.INSTANCE.getMachineCasing());
        itemConsumer.accept(Blocks.INSTANCE.getStorageMonitor());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getNetworkTransmitter());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getNetworkReceiver());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getSecurityManager());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getRelay());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getDiskInterface());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getAutocrafter());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getAutocrafterManager());
        appendDefaultBlockColor(consumer, Blocks.INSTANCE.getAutocraftingMonitor());
    }

    private static void appendDefaultBlockColor(final Consumer<ItemStack> consumer, final BlockColorMap<?, ?> map) {
        consumer.accept(new ItemStack(map.getDefault()));
    }

    public static void appendColoredVariants(final Consumer<ItemStack> consumer) {
        appendColoredBlocks(consumer, Blocks.INSTANCE.getController());
        if (Platform.INSTANCE.getConfig().isRequireEnergy()) {
            final var controllers = Items.INSTANCE.getControllers();
            for (int i = 1; i < controllers.size(); ++i) {
                consumer.accept(controllers.get(i).get().createAtEnergyCapacity());
            }
            appendColoredBlocks(consumer, Blocks.INSTANCE.getCreativeController());
        }
        appendColoredBlocks(consumer, Blocks.INSTANCE.getCable());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getImporter());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getExporter());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getExternalStorage());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getConstructor());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getDestructor());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getWirelessTransmitter());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getGrid());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getCraftingGrid());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getPatternGrid());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getDetector());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getNetworkTransmitter());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getNetworkReceiver());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getSecurityManager());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getRelay());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getDiskInterface());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getAutocrafter());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getAutocrafterManager());
        appendColoredBlocks(consumer, Blocks.INSTANCE.getAutocraftingMonitor());
    }

    private static void appendColoredBlocks(final Consumer<ItemStack> consumer, final BlockColorMap<?, ?> map) {
        map.forEach((color, id, block) -> {
            if (!map.isDefaultColor(color)) {
                consumer.accept(new ItemStack(block.get()));
            }
        });
    }

    private static void appendItems(final Consumer<ItemStack> consumer) {
        final boolean requireEnergy = Platform.INSTANCE.getConfig().isRequireEnergy();
        final Consumer<ItemLike> itemConsumer = item -> consumer.accept(new ItemStack(item));

        itemConsumer.accept(Items.INSTANCE.getQuartzEnrichedIron());
        itemConsumer.accept(Items.INSTANCE.getQuartzEnrichedCopper());
        itemConsumer.accept(Items.INSTANCE.getSilicon());
        itemConsumer.accept(Items.INSTANCE.getProcessorBinding());
        itemConsumer.accept(Items.INSTANCE.getWrench());

        Arrays.stream(ProcessorItem.Type.values()).map(Items.INSTANCE::getProcessor).forEach(itemConsumer);

        itemConsumer.accept(Items.INSTANCE.getConstructionCore());
        itemConsumer.accept(Items.INSTANCE.getDestructionCore());

        Arrays.stream(ItemStorageVariant.values())
            .filter(variant -> variant != ItemStorageVariant.CREATIVE)
            .map(Items.INSTANCE::getItemStoragePart)
            .forEach(itemConsumer);
        Arrays.stream(FluidStorageVariant.values())
            .filter(variant -> variant != FluidStorageVariant.CREATIVE)
            .map(Items.INSTANCE::getFluidStoragePart)
            .forEach(itemConsumer);

        Arrays.stream(ItemStorageVariant.values()).forEach(variant -> itemConsumer.accept(
            Items.INSTANCE.getItemStorageDisk(variant)
        ));
        Arrays.stream(FluidStorageVariant.values()).forEach(variant -> itemConsumer.accept(
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
        itemConsumer.accept(Items.INSTANCE.getAutocraftingUpgrade());
        itemConsumer.accept(Items.INSTANCE.getWirelessGrid());
        if (requireEnergy) {
            consumer.accept(Items.INSTANCE.getWirelessGrid().createAtEnergyCapacity());
            itemConsumer.accept(Items.INSTANCE.getCreativeWirelessGrid());
        }
        itemConsumer.accept(Items.INSTANCE.getConfigurationCard());
        itemConsumer.accept(Items.INSTANCE.getNetworkCard());
        itemConsumer.accept(Items.INSTANCE.getSecurityCard());
        itemConsumer.accept(Items.INSTANCE.getFallbackSecurityCard());
        itemConsumer.accept(Items.INSTANCE.getPattern());
        itemConsumer.accept(Items.INSTANCE.getWirelessAutocraftingMonitor());
        if (requireEnergy) {
            consumer.accept(Items.INSTANCE.getWirelessAutocraftingMonitor().createAtEnergyCapacity());
            itemConsumer.accept(Items.INSTANCE.getCreativeWirelessAutocraftingMonitor());
        }
    }
}
