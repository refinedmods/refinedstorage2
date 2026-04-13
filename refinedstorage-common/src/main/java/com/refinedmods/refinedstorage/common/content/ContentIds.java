package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class ContentIds {
    public static final Identifier CREATIVE_MODE_TAB = createIdentifier("general");
    public static final Identifier COLORED_CREATIVE_MODE_TAB = createIdentifier("colored");

    public static final Identifier DISK_DRIVE = createIdentifier("disk_drive");
    public static final Identifier MACHINE_CASING = createIdentifier("machine_casing");
    public static final Identifier CABLE = createIdentifier("cable");
    public static final Identifier QUARTZ_ENRICHED_IRON = createIdentifier("quartz_enriched_iron");
    public static final Identifier QUARTZ_ENRICHED_COPPER = createIdentifier("quartz_enriched_copper");
    public static final Identifier SILICON = createIdentifier("silicon");
    public static final Identifier PROCESSOR_BINDING = createIdentifier("processor_binding");
    public static final Identifier WRENCH = createIdentifier("wrench");
    public static final Identifier STORAGE_HOUSING = createIdentifier("storage_housing");
    public static final Identifier GRID = createIdentifier("grid");
    public static final Identifier CRAFTING_GRID = createIdentifier("crafting_grid");
    public static final Identifier PATTERN_GRID = createIdentifier("pattern_grid");
    public static final Identifier CONTROLLER = createIdentifier("controller");
    public static final Identifier CREATIVE_CONTROLLER = createIdentifier("creative_controller");
    public static final Identifier CONSTRUCTION_CORE = createIdentifier("construction_core");
    public static final Identifier DESTRUCTION_CORE = createIdentifier("destruction_core");
    public static final Identifier ITEM_STORAGE_BLOCK = createIdentifier("item_storage_block");
    public static final Identifier FLUID_STORAGE_BLOCK = createIdentifier("fluid_storage_block");
    public static final Identifier STORAGE_BLOCK = createIdentifier("storage_block");
    public static final Identifier IMPORTER = createIdentifier("importer");
    public static final Identifier EXPORTER = createIdentifier("exporter");
    public static final Identifier UPGRADE = createIdentifier("upgrade");
    public static final Identifier SPEED_UPGRADE = createIdentifier("speed_upgrade");
    public static final Identifier STACK_UPGRADE = createIdentifier("stack_upgrade");
    public static final Identifier FORTUNE_1_UPGRADE = createIdentifier("fortune_1_upgrade");
    public static final Identifier FORTUNE_2_UPGRADE = createIdentifier("fortune_2_upgrade");
    public static final Identifier FORTUNE_3_UPGRADE = createIdentifier("fortune_3_upgrade");
    public static final Identifier SILK_TOUCH_UPGRADE = createIdentifier("silk_touch_upgrade");
    public static final Identifier REGULATOR_UPGRADE = createIdentifier("regulator_upgrade");
    public static final Identifier INTERFACE = createIdentifier("interface");
    public static final Identifier EXTERNAL_STORAGE = createIdentifier("external_storage");
    public static final Identifier DETECTOR = createIdentifier("detector");
    public static final Identifier DESTRUCTOR = createIdentifier("destructor");
    public static final Identifier CONSTRUCTOR = createIdentifier("constructor");
    public static final Identifier WIRELESS_GRID = createIdentifier("wireless_grid");
    public static final Identifier CREATIVE_WIRELESS_GRID = createIdentifier("creative_wireless_grid");
    public static final Identifier WIRELESS_TRANSMITTER = createIdentifier("wireless_transmitter");
    public static final Identifier RANGE_UPGRADE = createIdentifier("range_upgrade");
    public static final Identifier CREATIVE_RANGE_UPGRADE = createIdentifier("creative_range_upgrade");
    public static final Identifier AUTOCRAFTING_UPGRADE = createIdentifier("autocrafting_upgrade");
    public static final Identifier STORAGE_MONITOR = createIdentifier("storage_monitor");
    public static final Identifier CONFIGURATION_CARD = createIdentifier("configuration_card");
    public static final Identifier NETWORK_RECEIVER = createIdentifier("network_receiver");
    public static final Identifier NETWORK_CARD = createIdentifier("network_card");
    public static final Identifier NETWORK_TRANSMITTER = createIdentifier("network_transmitter");
    public static final Identifier PORTABLE_GRID = createIdentifier("portable_grid");
    public static final Identifier CREATIVE_PORTABLE_GRID = createIdentifier("creative_portable_grid");
    public static final Identifier SECURITY_CARD = createIdentifier("security_card");
    public static final Identifier FALLBACK_SECURITY_CARD = createIdentifier("fallback_security_card");
    public static final Identifier SECURITY_MANAGER = createIdentifier("security_manager");
    public static final Identifier RELAY = createIdentifier("relay");
    public static final Identifier DISK_INTERFACE = createIdentifier("disk_interface");
    public static final Identifier PATTERN = createIdentifier("pattern");
    public static final Identifier AUTOCRAFTER = createIdentifier("autocrafter");
    public static final Identifier AUTOCRAFTER_MANAGER = createIdentifier("autocrafter_manager");
    public static final Identifier AUTOCRAFTING_MONITOR = createIdentifier("autocrafting_monitor");
    public static final Identifier WIRELESS_AUTOCRAFTING_MONITOR = createIdentifier(
        "wireless_autocrafting_monitor"
    );
    public static final Identifier CREATIVE_WIRELESS_AUTOCRAFTING_MONITOR = createIdentifier(
        "creative_wireless_autocrafting_monitor"
    );
    public static final Identifier DEBUG_STICK = createIdentifier("debug_stick");

    private ContentIds() {
    }

    public static Identifier forItemStoragePart(final ItemStorageVariant variant) {
        return createIdentifier(variant.getName() + "_storage_part");
    }

    public static Identifier forItemStorageBlock(final ItemStorageVariant variant) {
        return createIdentifier(variant.getName() + "_storage_block");
    }

    public static Identifier forFluidStoragePart(final FluidStorageVariant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_part");
    }

    public static Identifier forFluidStorageBlock(final FluidStorageVariant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_block");
    }

    public static Identifier forProcessor(final ProcessorItem.Type type) {
        return createIdentifier(type.getName() + "_processor");
    }

    public static Identifier forStorageDisk(final ItemStorageVariant variant) {
        return createIdentifier(variant.getName() + "_storage_disk");
    }

    public static Identifier forFluidStorageDisk(final FluidStorageVariant variant) {
        return createIdentifier(variant.getName() + "_fluid_storage_disk");
    }
}
