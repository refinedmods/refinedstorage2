package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.item.Rs2TagInsensitiveItemStackIdentifier;
import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;

public class ItemFilter extends Filter<Rs2ItemStack> {
    @Override
    protected StackList<Rs2ItemStack> createList(boolean exact) {
        return exact ? ItemStackList.create() : new ItemStackList<>(Rs2TagInsensitiveItemStackIdentifier::new);
    }
}
