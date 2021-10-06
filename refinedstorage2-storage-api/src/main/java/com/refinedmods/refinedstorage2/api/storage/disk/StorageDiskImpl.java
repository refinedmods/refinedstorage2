package com.refinedmods.refinedstorage2.api.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;

import java.util.Collection;

public class StorageDiskImpl<T> implements StorageDisk<T> {
    private final ResourceList<T> list = new ResourceListImpl<>();
    private final long capacity;
    private long stored;

    public StorageDiskImpl(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public long extract(T resource, long amount, Action action) {
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
    public long insert(T resource, long amount, Action action) {
        ResourceAmount.validate(resource, amount);

        if (capacity >= 0 && stored + amount > capacity) {
            return insertPartly(resource, capacity - stored, amount - (capacity - stored), action);
        } else {
            insertCompletely(resource, amount, action);
            return 0;
        }
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return list.getAll();
    }

    private long insertPartly(T resource, long amount, long remainder, Action action) {
        if (action == Action.EXECUTE && amount > 0) {
            stored += amount;
            list.add(resource, amount);
        }

        return remainder;
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

    @Override
    public long getCapacity() {
        return capacity;
    }
}
