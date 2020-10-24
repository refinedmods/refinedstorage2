package com.refinedmods.refinedstorage2.core.list;

import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface StackList<T> {
    StackListResult<T> add(T template, int amount);

    Optional<StackListResult<T>> remove(T template, int amount);

    Optional<ItemStack> get(T template);

    Optional<ItemStack> get(UUID id);

    Collection<T> getAll();
}
