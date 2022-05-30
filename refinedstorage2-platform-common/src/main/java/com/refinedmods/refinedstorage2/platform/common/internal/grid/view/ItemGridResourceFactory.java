
package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class ItemGridResourceFactory implements Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> {
    @Override
    public GridResource<ItemResource> apply(ResourceAmount<ItemResource> resourceAmount) {
        Item item = resourceAmount.getResource().getItem();
        ItemStack itemStack = resourceAmount.getResource().toItemStack();

        String name = item.getDescription().getString();
        String modId = getModId(itemStack);
        String modName = getModName(modId).orElse("");

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
        return Registry.ITEM.getResourceKey(item)
                .flatMap(Registry.ITEM::getHolder)
                .stream()
                .flatMap(Holder::tags)
                .map(tagKey -> tagKey.location().getPath())
                .collect(Collectors.toSet());
    }

    public abstract String getModId(ItemStack itemStack);

    public abstract Optional<String> getModName(String modId);
}
