package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Set;

import net.minecraft.item.Item;

public class ItemGridStack extends GridStack<ItemResource> {
    private final int id;

    public ItemGridStack(ResourceAmount<ItemResource> resourceAmount, String name, String modId, String modName, Set<String> tags) {
        super(resourceAmount, name, modId, modName, tags);
        this.id = Item.getRawId(getResourceAmount().getResource().getItem());
    }

    @Override
    public int getId() {
        return id;
    }
}
