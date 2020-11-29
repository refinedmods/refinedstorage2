package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Optional;

public interface Storage<T> {
    Optional<T> extract(T template, int amount, Action action);

    Optional<T> insert(T template, int amount, Action action);

    StackList<T> getStacks();

    int getStored();
}
