package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public abstract class ProxyStackList<R> implements StackList<R> {
    private final StackList<R> parent;

    public ProxyStackList(StackList<R> parent) {
        this.parent = parent;
    }

    @Override
    public StackListResult<R> add(R resource, long amount) {
        return parent.add(resource, amount);
    }

    @Override
    public Optional<StackListResult<R>> remove(R resource, long amount) {
        return parent.remove(resource, amount);
    }

    @Override
    public Optional<ResourceAmount<R>> get(R resource) {
        return parent.get(resource);
    }

    @Override
    public Optional<ResourceAmount<R>> get(UUID id) {
        return parent.get(id);
    }

    @Override
    public Collection<ResourceAmount<R>> getAll() {
        return parent.getAll();
    }

    @Override
    public void clear() {
        parent.clear();
    }
}
