package com.refinedmods.refinedstorage.api.storage.channel;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.listenable.ListenableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class StorageChannelImpl implements StorageChannel {
    protected final CompositeStorageImpl storage;
    private final ListenableResourceList list;

    public StorageChannelImpl() {
        this(new ResourceListImpl());
    }

    public StorageChannelImpl(final ResourceList list) {
        this.list = new ListenableResourceList(list);
        this.storage = new CompositeStorageImpl(this.list);
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
    public void addListener(final ResourceListListener listener) {
        list.addListener(listener);
    }

    @Override
    public void removeListener(final ResourceListListener listener) {
        list.removeListener(listener);
    }

    @Override
    public Optional<ResourceAmount> get(final ResourceKey resource) {
        return list.get(resource);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.insert(resource, amount, action, actor);
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
}
