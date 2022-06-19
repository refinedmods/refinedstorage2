package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorItem;

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
    public static final ResourceLocation FLUID_GRID = createIdentifier("fluid_grid");
    public static final ResourceLocation CONTROLLER = createIdentifier("controller");
    public static final ResourceLocation CREATIVE_CONTROLLER = createIdentifier("creative_controller");
    public static final ResourceLocation CONSTRUCTION_CORE = createIdentifier("construction_core");
    public static final ResourceLocation DESTRUCTION_CORE = createIdentifier("destruction_core");
    public static final ResourceLocation ITEM_STORAGE_BLOCK = createIdentifier("item_storage_block");
    public static final ResourceLocation FLUID_STORAGE_BLOCK = createIdentifier("fluid_storage_block");
    public static final ResourceLocation STORAGE_BLOCK = createIdentifier("storage_block");

    public static ResourceLocation forItemStoragePart(ItemStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_storage_part");
    }

    public static ResourceLocation forItemStorageBlock(ItemStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_storage_block");
    }

    public static ResourceLocation forFluidStoragePart(FluidStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_part");
    }

    public static ResourceLocation forFluidStorageBlock(FluidStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_block");
    }

    public static ResourceLocation forProcessor(ProcessorItem.Type type) {
        return createIdentifier(type.getName() + "_processor");
    }

    public static ResourceLocation forStorageDisk(ItemStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_storage_disk");
    }

    public static ResourceLocation forFluidStorageDisk(FluidStorageType.Variant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_disk");
    }

    private ContentIds() {
    }
}
