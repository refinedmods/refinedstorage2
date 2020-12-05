package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class EmptyItemStorage implements Storage<ItemStack> {
    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Action action) {
        template = template.copy();
        template.setCount(amount);
        return Optional.of(template);
    }

    @Override
    public Collection<ItemStack> getStacks() {
        return Collections.emptyList();
    }

    @Override
    public int getStored() {
        return 0;
    }
}
