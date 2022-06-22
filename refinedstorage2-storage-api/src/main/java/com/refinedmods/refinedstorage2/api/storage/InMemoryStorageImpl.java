package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;

import java.util.Collection;

import org.apiguardian.api.API;

/**
 * An implementation of a {@link Storage} which has an in-memory resource list as a backing list.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class InMemoryStorageImpl<T> implements Storage<T> {
    private final ResourceList<T> list = new ResourceListImpl<>();
    private long stored;

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        ResourceAmount.validate(resource, amount);

        return list.get(resource).map(resourceAmount -> {
            if (amount > resourceAmount.getAmount()) {
                return extractCompletely(resourceAmount, action);
            } else {
                return extractPartly(resource, amount, action);
            }
        }).orElse(0L);
    }

    private long extractPartly(T resource, long amount, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(resource, amount);
            stored -= amount;
        }

        return amount;
    }

    private long extractCompletely(ResourceAmount<T> resourceAmount, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(resourceAmount.getResource(), resourceAmount.getAmount());
            stored -= resourceAmount.getAmount();
        }

        return resourceAmount.getAmount();
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        ResourceAmount.validate(resource, amount);
        insertCompletely(resource, amount, action);
        return amount;
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return list.getAll();
    }

    private void insertCompletely(T template, long amount, Action action) {
        if (action == Action.EXECUTE) {
            stored += amount;
            list.add(template, amount);
        }
    }

    @Override
    public long getStored() {
        return stored;
    }
}
