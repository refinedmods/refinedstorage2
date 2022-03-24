package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

/**
 * This class can decorate any other {@link Storage} to add a capacity to it.
 * {@link InsertableStorage#insert(Object, long, Action, Source)} operations will respect this capacity.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class CappedStorage<T> extends ProxyStorage<T> implements CapacityAccessor {
    private final long capacity;

    /**
     * @param parent   the parent storage
     * @param capacity the capacity, must be 0 or larger than 0
     */
    public CappedStorage(Storage<T> parent, long capacity) {
        super(parent);
        Preconditions.checkArgument(capacity >= 0, "Capacity must be 0 or larger than 0");
        this.capacity = capacity;
    }

    /**
     * Constructs the capped storage with a {@link InMemoryStorageImpl} storage.
     *
     * @param capacity the capacity, must be 0 or larger than 0
     */
    public CappedStorage(long capacity) {
        this(new InMemoryStorageImpl<>(), capacity);
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        Preconditions.checkArgument(amount > 0, "Amount must be larger than 0");
        if (parent.getStored() + amount > capacity) {
            return insertPartly(resource, action, source);
        } else {
            return super.insert(resource, amount, action, source);
        }
    }

    private long insertPartly(T resource, Action action, Source source) {
        long spaceRemainingInStorage = capacity - parent.getStored();
        if (spaceRemainingInStorage == 0) {
            return 0;
        }
        return super.insert(resource, spaceRemainingInStorage, action, source);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }
}
