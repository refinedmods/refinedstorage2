package com.refinedmods.refinedstorage2.core.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.util.Action;

public class EmptyItemStorage implements Storage<Rs2ItemStack> {
    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        return Optional.empty();
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        template = template.copy();
        template.setAmount(amount);
        return Optional.of(template);
    }

    @Override
    public Collection<Rs2ItemStack> getStacks() {
        return Collections.emptyList();
    }

    @Override
    public long getStored() {
        return 0;
    }
}
