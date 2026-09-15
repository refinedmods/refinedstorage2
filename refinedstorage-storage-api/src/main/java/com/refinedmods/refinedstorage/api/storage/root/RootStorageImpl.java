package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.listenable.ListenableResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class RootStorageImpl implements RootStorage {
    protected final CompositeStorageImpl storage;
    private final ListenableResourceList list;
    private final Set<RootStorageListener> listeners;

    public RootStorageImpl() {
        this(MutableResourceListImpl.create(), new HashSet<>());
    }

    public RootStorageImpl(final MutableResourceList list) {
        this(list, new HashSet<>());
    }

    public RootStorageImpl(final MutableResourceList list, final Set<RootStorageListener> listeners) {
        this.list = new ListenableResourceList(list);
        this.storage = new CompositeStorageImpl(this.list);
        this.listeners = listeners;
    }

    @Override
    public void sortSources() {
        storage.sortSources();
    }

    @Override
    public void addSource(final Storage source) {
        storage.addSource(source);
    }

    @Override
    public void removeSource(final Storage source) {
        storage.removeSource(source);
    }

    @Override
    public boolean hasSource(final Predicate<Storage> matcher) {
        return storage.getSources().stream().anyMatch(matcher);
    }

    @Override
    public void addListener(final RootStorageListener listener) {
        list.addListener(listener);
        listeners.add(listener);
    }

    @Override
    public void removeListener(final RootStorageListener listener) {
        list.removeListener(listener);
        listeners.remove(listener);
    }

    @Override
    public long get(final ResourceKey resource) {
        return list.get(resource);
    }

    @Override
    public boolean contains(final ResourceKey resource) {
        return list.contains(resource);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        long totalIntercepted = 0;
        if (action == Action.EXECUTE) {
            for (final RootStorageListener listener : listeners) {
                final long available = amount - totalIntercepted;
                final long intercepted = listener.beforeInsert(resource, available);
                if (intercepted > available || intercepted < 0) {
                    throw new IllegalStateException(
                        "Intercepted %d while %d was available".formatted(intercepted, available)
                    );
                }
                totalIntercepted += intercepted;
                if (totalIntercepted == amount) {
                    return totalIntercepted;
                }
            }
        }
        final long inserted = storage.insert(resource, amount - totalIntercepted, action, actor);
        if (inserted > 0 && action == Action.EXECUTE) {
            notifyAfterInsertListeners(resource, inserted);
        }
        return inserted + totalIntercepted;
    }

    private void notifyAfterInsertListeners(final ResourceKey resource, final long inserted) {
        long available = inserted;
        for (final RootStorageListener listener : listeners) {
            final long reserved = listener.afterInsert(resource, available);
            if (reserved > available || reserved < 0) {
                throw new IllegalStateException("Reserved %d while %d was available".formatted(reserved, available));
            }
            available -= reserved;
            if (available == 0) {
                return;
            }
        }
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return storage.getAll();
    }

    @Override
    public long getStored() {
        return storage.getStored();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        return storage.findTrackedResourceByActorType(resource, actorType);
    }

    @Override
    public void collectTrackedResourcesByActorType(final Class<? extends Actor> actorType,
                                                   final Set<ResourceKey> resources,
                                                   final BiConsumer<ResourceKey, TrackedResource> consumer) {
        storage.collectTrackedResourcesByActorType(actorType, resources, consumer);
    }
}
