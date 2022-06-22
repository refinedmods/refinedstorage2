package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ProxyResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

/**
 * A resource list that can have listeners to track changes.
 * Can easily be used with an existing list by passing it in the constructor.
 * The {@link ResourceListListener#onChanged(ResourceListOperationResult)} method is only called when the change
 * is being performed through this list, not the delegate list.
 *
 * @param <T> the resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class ListenableResourceList<T> extends ProxyResourceList<T> {
    private final Set<ResourceListListener<T>> listeners = new HashSet<>();

    public ListenableResourceList(ResourceList<T> delegate) {
        super(delegate);
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

    public void addListener(ResourceListListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ResourceListListener<T> listener) {
        listeners.remove(listener);
    }
}
