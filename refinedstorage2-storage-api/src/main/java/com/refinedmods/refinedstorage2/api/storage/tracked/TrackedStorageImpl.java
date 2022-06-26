package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Optional;
import java.util.function.LongSupplier;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class TrackedStorageImpl<T> extends ProxyStorage<T> implements TrackedStorage<T> {
    private final TrackedStorageRepository<T> repository;
    private final LongSupplier clock;

    /**
     * A new tracked storage with an in-memory repository.
     *
     * @param delegate the storage that is being decorated
     * @param clock    a supplier for unix timestamps
     */
    public TrackedStorageImpl(final Storage<T> delegate, final LongSupplier clock) {
        this(delegate, new InMemoryTrackedStorageRepository<>(), clock);
    }

    /**
     * @param delegate   the storage that is being decorated
     * @param repository a repository for persisting and retrieving tracked resources
     * @param clock      a supplier for unix timestamps
     */
    public TrackedStorageImpl(final Storage<T> delegate, final TrackedStorageRepository<T> repository, final LongSupplier clock) {
        super(delegate);
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Source source) {
        Preconditions.checkNotNull(source);
        final long inserted = super.insert(resource, amount, action, source);
        if (inserted > 0 && action == Action.EXECUTE) {
            repository.update(resource, source, clock.getAsLong());
        }
        return inserted;
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Source source) {
        Preconditions.checkNotNull(source);
        final long extracted = super.extract(resource, amount, action, source);
        if (extracted > 0 && action == Action.EXECUTE) {
            repository.update(resource, source, clock.getAsLong());
        }
        return extracted;
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(final T resource, final Class<? extends Source> sourceType) {
        return repository.findTrackedResourceBySourceType(resource, sourceType);
    }
}
