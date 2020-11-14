package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.ItemStorageType;
import com.refinedmods.refinedstorage2.fabric.item.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class RefinedStorage2Items {
    private StorageHousingItem storageHousing;
    private final Map<ItemStorageType, StoragePartItem> storageParts = new HashMap<>();

    public void register(RefinedStorage2Blocks blocks, ItemGroup itemGroup) {
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "cable"), new BlockItem(blocks.getCable(), new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "quartz_enriched_iron"), new QuartzEnrichedIronItem(new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "quartz_enriched_iron_block"), new BlockItem(blocks.getQuartzEnrichedIron(), new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "silicon"), new SiliconItem(new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "processor_binding"), new ProcessorBindingItem(new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "disk_drive"), new BlockItem(blocks.getDiskDrive(), new Item.Settings().group(itemGroup)));
        storageHousing = Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "storage_housing"), new StorageHousingItem(new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "machine_casing"), new BlockItem(blocks.getMachineCasing(), new Item.Settings().group(itemGroup)));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, type.getName() + "_processor"), new ProcessorItem(new Item.Settings().group(itemGroup)));
        }

        for (ItemStorageType type : ItemStorageType.values()) {
            if (type != ItemStorageType.CREATIVE) {
                storageParts.put(type, Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, type.getName() + "_storage_part"), new StoragePartItem(new Item.Settings().group(itemGroup))));
            }
        }

        for (ItemStorageType type : ItemStorageType.values()) {
            Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, type.getName() + "_storage_disk"), new StorageDiskItem(new Item.Settings().group(itemGroup).maxCount(1).fireproof(), type));
        }
    }

    public StorageHousingItem getStorageHousing() {
        return storageHousing;
    }

    public StoragePartItem getStoragePart(ItemStorageType type) {
        return storageParts.get(type);
    }
}
