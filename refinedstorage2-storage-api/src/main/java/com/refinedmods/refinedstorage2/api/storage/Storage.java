package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Collection;

public interface Storage<T> {
    long extract(T resource, long amount, Action action);

    long insert(T resource, long amount, Action action);

    Collection<ResourceAmount<T>> getAll();

    long getStored();
}
