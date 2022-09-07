package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ListenableResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Collection;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class StorageChannelImpl<T> implements StorageChannel<T> {
    private final ListenableResourceList<T> list;
    private final CompositeStorage<T> storage;

    public StorageChannelImpl() {
        this(new ResourceListImpl<>());
    }

    public StorageChannelImpl(final ResourceList<T> list) {
        this.list = new ListenableResourceList<>(list);
        this.storage = new CompositeStorageImpl<>(list);
    }

    @Override
    public void sortSources() {
        storage.sortSources();
    }

    @Override
    public void addSource(final Storage<T> source) {
        storage.addSource(source);
    }

    @Override
    public void removeSource(final Storage<T> source) {
        storage.removeSource(source);
    }

    @Override
    public void addListener(final ResourceListListener<T> listener) {
        list.addListener(listener);
    }

    @Override
    public void removeListener(final ResourceListListener<T> listener) {
        list.removeListener(listener);
    }

    @Override
    public Optional<ResourceAmount<T>> get(final T resource) {
        return list.get(resource);
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        return storage.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        return storage.insert(resource, amount, action, actor);
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return storage.getAll();
    }

    @Override
    public long getStored() {
        return storage.getStored();
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return storage.findTrackedResourceByActorType(resource, actorType);
    }
}
