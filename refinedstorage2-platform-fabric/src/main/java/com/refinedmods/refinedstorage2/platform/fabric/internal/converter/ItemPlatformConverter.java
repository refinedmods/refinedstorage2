package com.refinedmods.refinedstorage2.platform.fabric.internal.converter;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.platform.fabric.api.converter.PlatformConverter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.item.FabricRs2Item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;

// TODO: This can be removed when Rs2Item is no longer an API concept.
public class ItemPlatformConverter implements PlatformConverter<Item, Rs2Item> {
    private final Map<Item, Rs2Item> itemCache = new HashMap<>();

    @Override
    public Item toPlatform(Rs2Item value) {
        return ((FabricRs2Item) value).getItem();
    }

    @Override
    public Rs2Item toDomain(Item value) {
        return itemCache.computeIfAbsent(value, FabricRs2Item::new);
    }
}
