package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Optional;

public interface Storage<T> {
    Optional<T> extract(T template, long amount, Action action);

    Optional<T> insert(T template, long amount, Action action);

    Collection<T> getStacks();

    long getStored();
}
