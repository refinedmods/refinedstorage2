package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk.ItemStorageType;
import com.refinedmods.refinedstorage2.fabric.item.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2Items {
    public void register(String namespace, RefinedStorage2Blocks blocks, ItemGroup itemGroup) {
        Registry.register(Registry.ITEM, new Identifier(namespace, "cable"), new BlockItem(blocks.getCable(), new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(namespace, "quartz_enriched_iron"), new QuartzEnrichedIronItem(new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(namespace, "quartz_enriched_iron_block"), new BlockItem(blocks.getQuartzEnrichedIron(), new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(namespace, "silicon"), new SiliconItem(new Item.Settings().group(itemGroup)));
        Registry.register(Registry.ITEM, new Identifier(namespace, "processor_binding"), new ProcessorBindingItem(new Item.Settings().group(itemGroup)));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            Registry.register(Registry.ITEM, new Identifier(namespace, type.getName() + "_processor"), new ProcessorItem(new Item.Settings().group(itemGroup)));
        }

        for (ItemStorageType type : ItemStorageType.values()) {
            if (type != ItemStorageType.CREATIVE) {
                Registry.register(Registry.ITEM, new Identifier(namespace, type.getName() + "_storage_part"), new StoragePartItem(new Item.Settings().group(itemGroup)));
            }
        }

        for (ItemStorageType type : ItemStorageType.values()) {
            Registry.register(Registry.ITEM, new Identifier(namespace, type.getName() + "_storage_disk"), new StorageDiskItem(new Item.Settings().group(itemGroup).maxCount(1)));
        }
    }
}
