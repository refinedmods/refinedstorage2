package com.refinedmods.refinedstorage2.api.storage.limited;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

/**
 * This class can decorate any other {@link Storage} to add a capacity to it.
 * {@link InsertableStorage#insert(Object, long, Action, Actor)} operations will respect this capacity.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class LimitedStorageImpl<T> extends AbstractProxyStorage<T> implements LimitedStorage<T> {
    private final long capacity;

    /**
     * @param delegate the storage that is being decorated
     * @param capacity the capacity, must be 0 or larger than 0
     */
    public LimitedStorageImpl(final Storage<T> delegate, final long capacity) {
        super(delegate);
        this.capacity = CoreValidations.validateNotNegative(capacity, "Capacity cannot be negative");
    }

    /**
     * Constructs the limited storage with a {@link InMemoryStorageImpl} storage.
     *
     * @param capacity the capacity, must be 0 or larger than 0
     */
    public LimitedStorageImpl(final long capacity) {
        this(new InMemoryStorageImpl<>(), capacity);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        CoreValidations.validateLargerThanZero(amount, "Amount must be larger than 0");
        if (delegate.getStored() + amount > capacity) {
            return insertPartly(resource, action, actor);
        } else {
            return super.insert(resource, amount, action, actor);
        }
    }

    private long insertPartly(final T resource, final Action action, final Actor actor) {
        final long spaceRemainingInStorage = capacity - delegate.getStored();
        if (spaceRemainingInStorage == 0) {
            return 0;
        }
        return super.insert(resource, spaceRemainingInStorage, action, actor);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }
}
