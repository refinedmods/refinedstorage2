package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.item.ProcessorBindingItem;
import com.refinedmods.refinedstorage2.fabric.item.ProcessorItem;
import com.refinedmods.refinedstorage2.fabric.item.QuartzEnrichedIronItem;
import com.refinedmods.refinedstorage2.fabric.item.SiliconItem;
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
    }
}
