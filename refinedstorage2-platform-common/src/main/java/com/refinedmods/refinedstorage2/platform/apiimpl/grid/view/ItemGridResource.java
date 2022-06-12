package com.refinedmods.refinedstorage2.platform.apiimpl.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.Map;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemGridResource extends GridResource<ItemResource> {
    private final int id;
    private final ItemStack itemStack;

    public ItemGridResource(ResourceAmount<ItemResource> resourceAmount, ItemStack itemStack, String name, String modId, String modName, Set<String> tags, String tooltip) {
        super(resourceAmount, name, Map.of(
                GridResourceAttributeKeys.MOD_ID, Set.of(modId),
                GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
                GridResourceAttributeKeys.TAGS, tags,
                GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.id = Item.getId(getResourceAmount().getResource().getItem());
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public int getId() {
        return id;
    }
}
