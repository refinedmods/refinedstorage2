package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.StackListImpl;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2TagInsensitiveItemStackIdentifier;

public class ItemFilter extends Filter<Rs2ItemStack> {
    @Override
    protected StackList<Rs2ItemStack> createList(boolean exact) {
        return exact ? StackListImpl.createItemStackList() : new StackListImpl<>(Rs2TagInsensitiveItemStackIdentifier::new);
    }
}
