package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public abstract class ProxyResourceList<T> implements ResourceList<T> {
    private final ResourceList<T> parent;

    public ProxyResourceList(ResourceList<T> parent) {
        this.parent = parent;
    }

    @Override
    public ResourceListOperationResult<T> add(T resource, long amount) {
        return parent.add(resource, amount);
    }

    @Override
    public Optional<ResourceListOperationResult<T>> remove(T resource, long amount) {
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
