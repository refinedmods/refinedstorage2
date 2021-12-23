package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemGridResourceFactory implements Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> {
    @Override
    public GridResource<ItemResource> apply(ResourceAmount<ItemResource> resourceAmount) {
        Item item = resourceAmount.getResource().getItem();
        ItemStack itemStack = resourceAmount.getResource().toItemStack();

        String name = item.getDescription().getString();
        String modId = getModId(item);
        String modName = getModName(modId);

        Set<String> tags = getTags(item);
        String tooltip = getTooltip(itemStack);

        return new ItemGridResource(resourceAmount, itemStack, name, modId, modName, tags, tooltip);
    }

    private String getTooltip(ItemStack itemStack) {
        return itemStack
                .getTooltipLines(null, TooltipFlag.Default.ADVANCED)
                .stream()
                .map(Component::getContents)
                .collect(Collectors.joining("\n"));
    }

    private Set<String> getTags(Item item) {
        return ItemTags
                .getAllTags()
                .getMatchingTags(item)
                .stream()
                .map(ResourceLocation::getPath)
                .collect(Collectors.toSet());
    }

    private String getModName(String modId) {
        return FabricLoader
                .getInstance()
                .getModContainer(modId)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getName)
                .orElse("");
    }

    private String getModId(Item item) {
        return Registry.ITEM.getKey(item).getNamespace();
    }
}
