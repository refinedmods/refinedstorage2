package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

import org.apiguardian.api.API;

/**
 * Represents a storage that can be inserted into.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface InsertableStorage<T> {
    /**
     * Inserts a resource into a storage.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @param action   the mode of insertion
     * @param source   the source
     * @return the amount inserted
     */
    long insert(T resource, long amount, Action action, Source source);
}
