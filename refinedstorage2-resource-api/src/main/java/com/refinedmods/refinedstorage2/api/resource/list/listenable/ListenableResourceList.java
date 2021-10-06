package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ProxyResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

import java.util.Optional;
import java.util.Set;

public class ListenableResourceList<T> extends ProxyResourceList<T> {
    private final Set<ResourceListListener<T>> listeners;

    public ListenableResourceList(ResourceList<T> parent, Set<ResourceListListener<T>> listeners) {
        super(parent);
        this.listeners = listeners;
    }

    @Override
    public ResourceListOperationResult<T> add(T resource, long amount) {
        ResourceListOperationResult<T> result = super.add(resource, amount);
        listeners.forEach(listener -> listener.onChanged(result));
        return result;
    }

    @Override
    public Optional<ResourceListOperationResult<T>> remove(T resource, long amount) {
        return super.remove(resource, amount)
                .map(result -> {
                    listeners.forEach(listener -> listener.onChanged(result));
                    return result;
                });
    }
}
