package com.refinedmods.refinedstorage2.api.stack.filter;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2TagInsensitiveItemStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;

public class ItemFilter extends Filter<Rs2ItemStack> {
    @Override
    protected StackList<Rs2ItemStack> createList(boolean exact) {
        return exact ? StackListImpl.createItemStackList() : new StackListImpl<>(Rs2TagInsensitiveItemStackIdentifier::new);
    }
}
