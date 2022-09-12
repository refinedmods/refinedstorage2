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
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        ResourceAmount.validate(resource, amount);

        return list.get(resource).map(resourceAmount -> {
            final long maxExtract = Math.min(
                resourceAmount.getAmount(),
                amount
            );
            return doExtract(resource, maxExtract, action);
        }).orElse(0L);
    }

    private long doExtract(final T resource, final long amount, final Action action) {
        if (action == Action.EXECUTE) {
            list.remove(resource, amount);
            stored -= amount;
        }
        return amount;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        ResourceAmount.validate(resource, amount);
        if (action == Action.EXECUTE) {
            stored += amount;
            list.add(resource, amount);
        }
        return amount;
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return list.getAll();
    }

    @Override
    public long getStored() {
        return stored;
    }
}
