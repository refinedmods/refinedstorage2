package com.refinedmods.refinedstorage2.platform.fabric.init;

import com.refinedmods.refinedstorage2.platform.fabric.FeatureFlag;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.item.CoreItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.ItemStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.ProcessorBindingItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.QuartzEnrichedIronItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.SiliconItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.StoragePartItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.WrenchItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.block.ControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.fabric.item.block.NameableBlockItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class Rs2Items {
    private final Map<ItemStorageDiskItem.ItemStorageType, StoragePartItem> storageParts = new EnumMap<>(ItemStorageDiskItem.ItemStorageType.class);
    private final Map<FluidStorageDiskItem.FluidStorageType, FluidStoragePartItem> fluidStorageParts = new EnumMap<>(FluidStorageDiskItem.FluidStorageType.class);
    private final List<ControllerBlockItem> controllers = new ArrayList<>();
    private StorageHousingItem storageHousing;

    public void register(Rs2Blocks blocks, CreativeModeTab itemGroup) {
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("cable"), new BlockItem(blocks.getCable(), createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("quartz_enriched_iron"), new QuartzEnrichedIronItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("quartz_enriched_iron_block"), new BlockItem(blocks.getQuartzEnrichedIron(), createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("silicon"), new SiliconItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("processor_binding"), new ProcessorBindingItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("disk_drive"), new BlockItem(blocks.getDiskDrive(), createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("wrench"), new WrenchItem(createSettings(itemGroup).stacksTo(1)));

        if (Rs2Mod.FEATURES.contains(FeatureFlag.RELAY)) {
            Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("relay"), new BlockItem(blocks.getRelay(), createSettings(itemGroup)));
        }

        storageHousing = Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("storage_housing"), new StorageHousingItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("machine_casing"), new BlockItem(blocks.getMachineCasing(), createSettings(itemGroup)));
        blocks.getGrid().forEach((color, block) -> Registry.register(
                Registry.ITEM,
                blocks.getGrid().getId(color, "grid"),
                new NameableBlockItem(
                        block,
                        createSettings(itemGroup),
                        color,
                        blocks.getGrid().getName(color, Rs2Mod.createTranslation(Rs2Blocks.BLOCK_TRANSLATION_CATEGORY, "grid"))
                )
        ));
        blocks.getFluidGrid().forEach((color, block) -> Registry.register(
                Registry.ITEM,
                blocks.getFluidGrid().getId(color, "fluid_grid"),
                new NameableBlockItem(
                        block,
                        createSettings(itemGroup),
                        color,
                        blocks.getFluidGrid().getName(color, Rs2Mod.createTranslation(Rs2Blocks.BLOCK_TRANSLATION_CATEGORY, "fluid_grid"))
                )
        ));
        blocks.getController().forEach((color, block) -> controllers.add(Registry.register(
                Registry.ITEM,
                blocks.getController().getId(color, "controller"),
                new ControllerBlockItem(
                        block,
                        createSettings(itemGroup).stacksTo(1),
                        color,
                        blocks.getController().getName(color, Rs2Mod.createTranslation(Rs2Blocks.BLOCK_TRANSLATION_CATEGORY, "controller"))
                )
        )));
        blocks.getCreativeController().forEach((color, block) -> Registry.register(
                Registry.ITEM,
                blocks.getCreativeController().getId(color, "creative_controller"),
                new NameableBlockItem(
                        block,
                        createSettings(itemGroup).stacksTo(1),
                        color,
                        blocks.getCreativeController().getName(color, Rs2Mod.createTranslation(Rs2Blocks.BLOCK_TRANSLATION_CATEGORY, "creative_controller"))
                )
        ));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            Registry.register(Registry.ITEM, Rs2Mod.createIdentifier(type.getName() + "_processor"), new ProcessorItem(createSettings(itemGroup)));
        }

        for (ItemStorageDiskItem.ItemStorageType type : ItemStorageDiskItem.ItemStorageType.values()) {
            if (type != ItemStorageDiskItem.ItemStorageType.CREATIVE) {
                storageParts.put(type, Registry.register(Registry.ITEM, Rs2Mod.createIdentifier(type.getName() + "_storage_part"), new StoragePartItem(createSettings(itemGroup))));
            }
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            if (type != FluidStorageDiskItem.FluidStorageType.CREATIVE) {
                fluidStorageParts.put(type, Registry.register(Registry.ITEM, Rs2Mod.createIdentifier(type.getName() + "_fluid_storage_part"), new FluidStoragePartItem(createSettings(itemGroup))));
            }
        }

        for (ItemStorageDiskItem.ItemStorageType type : ItemStorageDiskItem.ItemStorageType.values()) {
            Registry.register(Registry.ITEM, Rs2Mod.createIdentifier(type.getName() + "_storage_disk"), new ItemStorageDiskItem(createSettings(itemGroup).stacksTo(1).fireResistant(), type));
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            Registry.register(Registry.ITEM, Rs2Mod.createIdentifier(type.getName() + "_fluid_storage_disk"), new FluidStorageDiskItem(createSettings(itemGroup).stacksTo(1).fireResistant(), type));
        }

        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("construction_core"), new CoreItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, Rs2Mod.createIdentifier("destruction_core"), new CoreItem(createSettings(itemGroup)));
    }

    private Item.Properties createSettings(CreativeModeTab itemGroup) {
        return new Item.Properties().tab(itemGroup);
    }

    public List<ControllerBlockItem> getControllers() {
        return controllers;
    }

    public StorageHousingItem getStorageHousing() {
        return storageHousing;
    }

    public StoragePartItem getStoragePart(ItemStorageDiskItem.ItemStorageType type) {
        return storageParts.get(type);
    }

    public FluidStoragePartItem getFluidStoragePart(FluidStorageDiskItem.FluidStorageType type) {
        return fluidStorageParts.get(type);
    }
}
