package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * A storage channel is the entry-point for various storage operations for a given resource type.
 * It acts as a storage, and is usually backed by a {@link com.refinedmods.refinedstorage2.api.storage.composite.CompositeStorage}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface StorageChannel<T> extends Storage<T> {
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
     * Extracts a resource from the storage channel.
     * This will perform the extraction with {@link com.refinedmods.refinedstorage2.api.core.Action#EXECUTE}
     * and mark the resource as changed by the given source.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @param source   the source, may not be null
     * @return the amount extracted
     */
    long extract(T resource, long amount, Source source);

    /**
     * Inserts a resource into the storage channel.
     * This will perform the insertion with {@link com.refinedmods.refinedstorage2.api.core.Action#EXECUTE}
     * and mark the resource as changed by the given source.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @param source   the source, may not be null
     * @return the amount inserted
     */
    long insert(T resource, long amount, Source source);

    /**
     * @return the storage tracker for this storage channel
     */
    StorageTracker<T> getTracker();

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
    void addSource(Storage<?> source);

    /**
     * Removes a source from the channel.
     *
     * @param source the source
     */
    void removeSource(Storage<?> source);
}
