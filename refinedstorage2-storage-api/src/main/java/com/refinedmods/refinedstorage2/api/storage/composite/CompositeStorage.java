package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.List;

import org.apiguardian.api.API;

/**
 * This represents a single storage that can be backed by multiple storages.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface CompositeStorage<T> extends Storage<T>, TrackedStorage<T> {
    /**
     * Sorts storages that implement {@link Priority}.
     */
    void sortSources();

    /**
     * Adds a source and resorts them.
     *
     * @param source the source
     */
    void addSource(Storage<T> source);

    /**
     * Removes a source and resorts them.
     *
     * @param source the source
     */
    void removeSource(Storage<T> source);

    /**
     * @return an unmodifiable source list
     */
    List<Storage<T>> getSources();

    /**
     * Clears all sources.
     */
    void clearSources();
}
