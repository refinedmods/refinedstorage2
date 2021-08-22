package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

import java.util.Collection;
import java.util.Optional;

public class ProxyStorage<T> implements Storage<T> {
    protected Storage<T> parent;

    protected ProxyStorage(Storage<T> parent) {
        this.parent = parent;
    }

    @Override
    public Optional<T> extract(T template, long amount, Action action) {
        return parent.extract(template, amount, action);
    }

    @Override
    public Optional<T> insert(T template, long amount, Action action) {
        return parent.insert(template, amount, action);
    }

    @Override
    public Collection<T> getStacks() {
        return parent.getStacks();
    }

    @Override
    public long getStored() {
        return parent.getStored();
    }
}
