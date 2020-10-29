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

import java.util.HashMap;
import java.util.Map;

public class RefinedStorage2Items {
    private BlockItem cable;
    private QuartzEnrichedIronItem quartzEnrichedIron;
    private BlockItem quartzEnrichedIronBlock;
    private SiliconItem silicon;
    private ProcessorBindingItem processorBinding;
    private Map<ProcessorItem.Type, ProcessorItem> processors = new HashMap<>();

    public void register(String namespace, RefinedStorage2Blocks blocks, ItemGroup itemGroup) {
        cable = new BlockItem(blocks.getCable(), new Item.Settings().group(itemGroup));
        Registry.register(Registry.ITEM, new Identifier(namespace, "cable"), cable);

        quartzEnrichedIron = Registry.register(Registry.ITEM, new Identifier(namespace, "quartz_enriched_iron"), new QuartzEnrichedIronItem(new Item.Settings().group(itemGroup)));

        Registry.register(Registry.ITEM, new Identifier(namespace, "quartz_enriched_iron_block"), new BlockItem(blocks.getQuartzEnrichedIron(), new Item.Settings().group(itemGroup)));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            processors.put(type, Registry.register(Registry.ITEM, new Identifier(namespace, type.getName() + "_processor"), new ProcessorItem(new Item.Settings().group(itemGroup))));
        }

        silicon = Registry.register(Registry.ITEM, new Identifier(namespace, "silicon"), new SiliconItem(new Item.Settings().group(itemGroup)));
        processorBinding = Registry.register(Registry.ITEM, new Identifier(namespace, "processor_binding"), new ProcessorBindingItem(new Item.Settings().group(itemGroup)));
    }
}
