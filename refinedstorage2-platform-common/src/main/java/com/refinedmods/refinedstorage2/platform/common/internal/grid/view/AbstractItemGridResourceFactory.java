package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class AbstractItemGridResourceFactory implements GridResourceFactory {
    @Override
    @SuppressWarnings("unchecked")
    public Optional<GridResource> apply(final ResourceAmount<?> resourceAmount) {
        if (!(resourceAmount.getResource() instanceof ItemResource itemResource)) {
            return Optional.empty();
        }
        final Item item = itemResource.item();
        final ItemStack itemStack = itemResource.toItemStack();
        final String name = item.getDescription().getString();
        final String modId = getModId(itemStack);
        final String modName = getModName(modId).orElse("");
        final Set<String> tags = getTags(item);
        final String tooltip = getTooltip(itemStack);
        return Optional.of(new ItemGridResource(
            (ResourceAmount<ItemResource>) resourceAmount,
            itemStack,
            name,
            modId,
            modName,
            tags,
            tooltip
        ));
    }

    private String getTooltip(final ItemStack itemStack) {
        return itemStack
            .getTooltipLines(null, TooltipFlag.Default.ADVANCED)
            .stream()
            .map(Component::getString)
            .collect(Collectors.joining("\n"));
    }

    @SuppressWarnings("deprecation") // forge deprecates Registry access
    private Set<String> getTags(final Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item)
            .flatMap(BuiltInRegistries.ITEM::getHolder)
            .stream()
            .flatMap(Holder::tags)
            .map(tagKey -> tagKey.location().getPath())
            .collect(Collectors.toSet());
    }

    public abstract String getModId(ItemStack itemStack);

    public abstract Optional<String> getModName(String modId);
}
