package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.ItemStorageType;

import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public final class ContentIds {
    public static final ResourceLocation DISK_DRIVE = createIdentifier("disk_drive");
    public static final ResourceLocation MACHINE_CASING = createIdentifier("machine_casing");
    public static final ResourceLocation CABLE = createIdentifier("cable");
    public static final ResourceLocation QUARTZ_ENRICHED_IRON_BLOCK = createIdentifier("quartz_enriched_iron_block");
    public static final ResourceLocation QUARTZ_ENRICHED_IRON = createIdentifier("quartz_enriched_iron");
    public static final ResourceLocation SILICON = createIdentifier("silicon");
    public static final ResourceLocation PROCESSOR_BINDING = createIdentifier("processor_binding");
    public static final ResourceLocation WRENCH = createIdentifier("wrench");
    public static final ResourceLocation STORAGE_HOUSING = createIdentifier("storage_housing");
    public static final ResourceLocation GRID = createIdentifier("grid");
    public static final ResourceLocation CRAFTING_GRID = createIdentifier("crafting_grid");
    public static final ResourceLocation CONTROLLER = createIdentifier("controller");
    public static final ResourceLocation CREATIVE_CONTROLLER = createIdentifier("creative_controller");
    public static final ResourceLocation CONSTRUCTION_CORE = createIdentifier("construction_core");
    public static final ResourceLocation DESTRUCTION_CORE = createIdentifier("destruction_core");
    public static final ResourceLocation ITEM_STORAGE_BLOCK = createIdentifier("item_storage_block");
    public static final ResourceLocation FLUID_STORAGE_BLOCK = createIdentifier("fluid_storage_block");
    public static final ResourceLocation STORAGE_BLOCK = createIdentifier("storage_block");
    public static final ResourceLocation IMPORTER = createIdentifier("importer");
    public static final ResourceLocation EXPORTER = createIdentifier("exporter");
    public static final ResourceLocation UPGRADE = createIdentifier("upgrade");
    public static final ResourceLocation SPEED_UPGRADE = createIdentifier("speed_upgrade");
    public static final ResourceLocation STACK_UPGRADE = createIdentifier("stack_upgrade");
    public static final ResourceLocation FORTUNE_1_UPGRADE = createIdentifier("fortune_1_upgrade");
    public static final ResourceLocation FORTUNE_2_UPGRADE = createIdentifier("fortune_2_upgrade");
    public static final ResourceLocation FORTUNE_3_UPGRADE = createIdentifier("fortune_3_upgrade");
    public static final ResourceLocation SILK_TOUCH_UPGRADE = createIdentifier("silk_touch_upgrade");
    public static final ResourceLocation REGULATOR_UPGRADE = createIdentifier("regulator_upgrade");
    public static final ResourceLocation INTERFACE = createIdentifier("interface");
    public static final ResourceLocation EXTERNAL_STORAGE = createIdentifier("external_storage");
    public static final ResourceLocation DETECTOR = createIdentifier("detector");
    public static final ResourceLocation DESTRUCTOR = createIdentifier("destructor");
    public static final ResourceLocation CONSTRUCTOR = createIdentifier("constructor");
    public static final ResourceLocation WIRELESS_GRID = createIdentifier("wireless_grid");
    public static final ResourceLocation CREATIVE_WIRELESS_GRID = createIdentifier("creative_wireless_grid");
    public static final ResourceLocation WIRELESS_TRANSMITTER = createIdentifier("wireless_transmitter");
    public static final ResourceLocation RANGE_UPGRADE = createIdentifier("range_upgrade");
    public static final ResourceLocation CREATIVE_RANGE_UPGRADE = createIdentifier("creative_range_upgrade");
    public static final ResourceLocation STORAGE_MONITOR = createIdentifier("storage_monitor");
    public static final ResourceLocation CONFIGURATION_CARD = createIdentifier("configuration_card");
    public static final ResourceLocation NETWORK_RECEIVER = createIdentifier("network_receiver");

    private ContentIds() {
    }

    public static ResourceLocation forItemStoragePart(final ItemStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_storage_part");
    }

    public static ResourceLocation forItemStorageBlock(final ItemStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_storage_block");
    }

    public static ResourceLocation forFluidStoragePart(final FluidStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_part");
    }

    public static ResourceLocation forFluidStorageBlock(final FluidStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_block");
    }

    public static ResourceLocation forProcessor(final ProcessorItem.Type type) {
        return createIdentifier(type.getName() + "_processor");
    }

    public static ResourceLocation forStorageDisk(final ItemStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_storage_disk");
    }

    public static ResourceLocation forFluidStorageDisk(final FluidStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_disk");
    }
}
