package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * A storage channel is the entry-point for various storage operations for a given resource type.
 * It acts as a storage, and is usually backed by a
 * {@link com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageChannel<T> extends Storage<T>, TrackedStorage<T> {
    /**
     * Adds a listener to the storage channel.
     *
     * @param listener the listener
     */
    void addListener(ResourceListListener<T> listener);

    /**
     * Removes a listener from the storage channel.
     *
     * @param listener the listener
     */
    void removeListener(ResourceListListener<T> listener);

    /**
     * @param resource the resource to retrieve
     * @return the resource amount for the given resource, if present
     */
    Optional<ResourceAmount<T>> get(T resource);

    /**
     * Sorts the sources in the backing storage.
     */
    void sortSources();

    /**
     * Adds a source to the channel.
     *
     * @param source the source
     */
    void addSource(Storage<T> source);

    /**
     * Removes a source from the channel.
     *
     * @param source the source
     */
    void removeSource(Storage<T> source);

    /**
     * Checks if a source is present.
     *
     * @param matcher a predicate
     * @return whether the predicate matched
     */
    boolean hasSource(Predicate<Storage<T>> matcher);
}
