package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import net.minecraft.item.ItemStack;

public class ItemFilter extends Filter<ItemStack> {
    @Override
    protected StackList<ItemStack> createList(boolean exact) {
        return exact ? ItemStackList.create() : new ItemStackList<>(TagInsensitiveItemStackIdentifier::new);
    }
}
