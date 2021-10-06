package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack.ItemGridStack;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.Item;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemGridStackFactory implements Function<ResourceAmount<ItemResource>, GridStack<ItemResource>> {
    @Override
    public GridStack<ItemResource> apply(ResourceAmount<ItemResource> resourceAmount) {
        Item item = resourceAmount.getResource().getItem();
        String name = item.getName().getString();
        String modId = Registry.ITEM.getId(item).getNamespace();
        String modName = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("");
        Set<String> tags = ItemTags.getTagGroup().getTagsFor(item).stream().map(Identifier::getPath).collect(Collectors.toSet());

        return new ItemGridStack(resourceAmount, name, modId, modName, tags);
    }
}
