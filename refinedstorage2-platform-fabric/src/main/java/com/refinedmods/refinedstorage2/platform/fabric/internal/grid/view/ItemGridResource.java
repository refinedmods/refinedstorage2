package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Set;

import net.minecraft.item.Item;

public class ItemGridResource extends GridResource<ItemResource> {
    private final int id;

    public ItemGridResource(ResourceAmount<ItemResource> resourceAmount, String name, String modId, String modName, Set<String> tags) {
        super(resourceAmount, name, modId, modName, tags);
        this.id = Item.getRawId(getResourceAmount().getResource().getItem());
    }

    @Override
    public int getId() {
        return id;
    }
}
