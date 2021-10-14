package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemGridResource extends GridResource<ItemResource> {
    private final int id;
    private final ItemStack itemStack;

    public ItemGridResource(ResourceAmount<ItemResource> resourceAmount, String name, String modId, String modName, Set<String> tags) {
        super(resourceAmount, name, modId, modName, tags);
        this.id = Item.getRawId(getResourceAmount().getResource().getItem());
        this.itemStack = resourceAmount.getResource().toItemStack();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public int getId() {
        return id;
    }
}
