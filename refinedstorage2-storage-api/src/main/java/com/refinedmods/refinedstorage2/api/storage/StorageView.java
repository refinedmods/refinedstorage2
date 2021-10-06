package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;

public interface StorageView<T> {
    Collection<ResourceAmount<T>> getAll();

    long getStored();
}
