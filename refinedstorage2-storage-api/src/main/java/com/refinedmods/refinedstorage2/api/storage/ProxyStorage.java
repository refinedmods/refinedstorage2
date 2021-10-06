package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;

public class ProxyStorage<T> implements Storage<T> {
    protected Storage<T> parent;

    protected ProxyStorage(Storage<T> parent) {
        this.parent = parent;
    }

    @Override
    public long extract(T resource, long amount, Action action) {
        return parent.extract(resource, amount, action);
    }

    @Override
    public long insert(T resource, long amount, Action action) {
        return parent.insert(resource, amount, action);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return parent.getAll();
    }

    @Override
    public long getStored() {
        return parent.getStored();
    }
}
