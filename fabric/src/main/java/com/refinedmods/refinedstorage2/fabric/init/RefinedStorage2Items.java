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
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "cable"), new BlockItem(blocks.getCable(), createSettings(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "quartz_enriched_iron"), new QuartzEnrichedIronItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "quartz_enriched_iron_block"), new BlockItem(blocks.getQuartzEnrichedIron(), createSettings(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "silicon"), new SiliconItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "processor_binding"), new ProcessorBindingItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "disk_drive"), new BlockItem(blocks.getDiskDrive(), createSettings(itemGroup)));
        storageHousing = Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "storage_housing"), new StorageHousingItem(createSettings(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "machine_casing"), new BlockItem(blocks.getMachineCasing(), createSettings(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, "grid"), new BlockItem(blocks.getGrid(), createSettings(itemGroup)));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, type.getName() + "_processor"), new ProcessorItem(createSettings(itemGroup)));
        }

        for (ItemStorageType type : ItemStorageType.values()) {
            if (type != ItemStorageType.CREATIVE) {
                storageParts.put(type, Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, type.getName() + "_storage_part"), new StoragePartItem(createSettings(itemGroup))));
            }
        }

        for (ItemStorageType type : ItemStorageType.values()) {
            Registry.register(Registry.ITEM, new Identifier(RefinedStorage2Mod.ID, type.getName() + "_storage_disk"), new StorageDiskItem(createSettings(itemGroup).maxCount(1).fireproof(), type));
        }
    }

    private Item.Settings createSettings(ItemGroup itemGroup) {
        return new Item.Settings().group(itemGroup);
    }

    public StorageHousingItem getStorageHousing() {
        return storageHousing;
    }

    public StoragePartItem getStoragePart(ItemStorageType type) {
        return storageParts.get(type);
    }
}
