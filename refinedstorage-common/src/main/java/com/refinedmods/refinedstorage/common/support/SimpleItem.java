package com.refinedmods.refinedstorage.common.support;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class SimpleItem extends Item {
    public SimpleItem(final Identifier id) {
        super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
    }
}
