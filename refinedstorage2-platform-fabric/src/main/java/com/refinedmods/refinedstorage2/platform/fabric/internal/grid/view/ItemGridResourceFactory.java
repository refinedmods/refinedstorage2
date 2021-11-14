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
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemGridResourceFactory implements Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> {
    @Override
    public GridResource<ItemResource> apply(ResourceAmount<ItemResource> resourceAmount) {
        Item item = resourceAmount.getResource().getItem();
        ItemStack itemStack = resourceAmount.getResource().toItemStack();

        String name = item.getName().getString();
        String modId = getModId(item);
        String modName = getModName(modId);

        Set<String> tags = getTags(item);
        String tooltip = getTooltip(itemStack);

        return new ItemGridResource(resourceAmount, itemStack, name, modId, modName, tags, tooltip);
    }

    private String getTooltip(ItemStack itemStack) {
        return itemStack
                .getTooltip(null, TooltipContext.Default.ADVANCED)
                .stream()
                .map(Text::asString)
                .collect(Collectors.joining("\n"));
    }

    private Set<String> getTags(Item item) {
        return ItemTags
                .getTagGroup()
                .getTagsFor(item)
                .stream()
                .map(Identifier::getPath)
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
        return Registry.ITEM.getId(item).getNamespace();
    }
}
