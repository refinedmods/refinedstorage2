package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Optional;
import java.util.function.LongSupplier;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class TrackedStorageImpl extends AbstractProxyStorage implements TrackedStorage {
    private final TrackedStorageRepository repository;
    private final LongSupplier clock;

    /**
     * A new tracked storage with an in-memory repository.
     *
     * @param delegate the storage that is being decorated
     * @param clock    a supplier for unix timestamps
     */
    public TrackedStorageImpl(final Storage delegate, final LongSupplier clock) {
        this(delegate, new InMemoryTrackedStorageRepository(), clock);
    }

    /**
     * @param delegate   the storage that is being decorated
     * @param repository a repository for persisting and retrieving tracked resources
     * @param clock      a supplier for unix timestamps
     */
    public TrackedStorageImpl(final Storage delegate,
                              final TrackedStorageRepository repository,
                              final LongSupplier clock) {
        super(delegate);
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        CoreValidations.validateNotNull(actor, "Source must not be null");
        final long inserted = super.insert(resource, amount, action, actor);
        if (inserted > 0 && action == Action.EXECUTE) {
            repository.update(resource, actor, clock.getAsLong());
        }
        return inserted;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        CoreValidations.validateNotNull(actor, "Source must not be null");
        final long extracted = super.extract(resource, amount, action, actor);
        if (extracted > 0 && action == Action.EXECUTE) {
            repository.update(resource, actor, clock.getAsLong());
        }
        return extracted;
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return repository.findTrackedResourceByActorType(resource, actorType);
    }
}
