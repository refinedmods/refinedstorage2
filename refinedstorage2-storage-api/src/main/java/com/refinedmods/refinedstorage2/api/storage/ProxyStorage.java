package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

/**
 * This is a utility class to easily decorate a {@link Storage}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class ProxyStorage<T> implements Storage<T> {
    protected Storage<T> parent;

    /**
     * @param parent the parent storage, may not be null
     */
    protected ProxyStorage(Storage<T> parent) {
        Preconditions.checkNotNull(parent);
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
