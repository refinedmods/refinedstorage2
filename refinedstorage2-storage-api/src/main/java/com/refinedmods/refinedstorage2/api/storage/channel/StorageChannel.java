package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Optional;

public interface StorageChannel<T> extends Storage<T> {
    void addListener(ResourceListListener<T> listener);

    void removeListener(ResourceListListener<T> listener);

    long extract(T resource, long amount, Source source);

    long insert(T resource, long amount, Source source);

    StorageTracker<T> getTracker();

    Optional<ResourceAmount<T>> get(T resource);

    void sortSources();

    void addSource(Storage<?> source);

    void removeSource(Storage<?> source);

    void invalidate();
}
