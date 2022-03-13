package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

/**
 * This represents a single storage that can be backed by multiple storages.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface CompositeStorage<T> extends Storage<T> {
    /**
     * Sort the sources of this composite.
     * If a storage implements {@link Priority}, the composite will account for this.
     */
    void sortSources();

    /**
     * Adds a source and resorts the composite storage.
     *
     * @param source the source
     */
    void addSource(Storage<T> source);

    /**
     * Removes a source and resorts the composite storage.
     *
     * @param source the source
     */
    void removeSource(Storage<T> source);

    /**
     * Clears all sources.
     */
    void clearSources();

    /**
     * @param listener the listener
     */
    void addListener(CompositeStorageListener<T> listener);

    /**
     * @param listener the listener
     */
    void removeListener(CompositeStorageListener<T> listener);
}
