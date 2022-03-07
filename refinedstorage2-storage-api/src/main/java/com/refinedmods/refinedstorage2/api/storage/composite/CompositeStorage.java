package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface CompositeStorage<T> extends Storage<T> {
    void sortSources();

    void addSource(Storage<T> source);

    void removeSource(Storage<T> source);

    void addListener(CompositeStorageListener<T> listener);

    void removeListener(CompositeStorageListener<T> listener);
}
