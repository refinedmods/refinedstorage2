package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public abstract class ProxyStackList<T> implements StackList<T> {
    private final StackList<T> parent;

    public ProxyStackList(StackList<T> parent) {
        this.parent = parent;
    }

    @Override
    public StackListResult<T> add(T resource, long amount) {
        return parent.add(resource, amount);
    }

    @Override
    public Optional<StackListResult<T>> remove(T resource, long amount) {
        return parent.remove(resource, amount);
    }

    @Override
    public Optional<ResourceAmount<T>> get(T resource) {
        return parent.get(resource);
    }

    @Override
    public Optional<ResourceAmount<T>> get(UUID id) {
        return parent.get(id);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return parent.getAll();
    }

    @Override
    public void clear() {
        parent.clear();
    }
}
