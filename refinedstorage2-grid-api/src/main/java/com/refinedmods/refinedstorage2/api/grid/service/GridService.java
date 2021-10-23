package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

import org.apiguardian.api.API;

/**
 * The grid service is the service that the grid uses to interact with the storage network.
 *
 * @param <T> the resource type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface GridService<T> {
    /**
     * Tries to move a resource from the network storage to the destination.
     * The amount being extracted depends on the extraction mode.
     * The grid service will first try to insert the entire amount (depending on the extraction mode and state of the network storage)
     * into the destination (simulated), and will then attempt to insert the actual amount that there is space for (execute).
     *
     * @param resource    the resource
     * @param extractMode the extract mode
     * @param destination the destination
     */
    void extract(T resource, GridExtractMode extractMode, InsertableStorage<T> destination);

    /**
     * Tries to move a resource from the source to the network storage.
     * The amount being inserted depends on the insert mode.
     * The grid service will first try to extract the entire amount (depending on the insertion mode and state of the source storage)
     * into the network storage (simulated), and will then attempt to insert the actual amount that there is space for (execute).
     *
     * @param resource   the resource
     * @param insertMode the insertion mode
     * @param source     the source
     */
    void insert(T resource, GridInsertMode insertMode, ExtractableStorage<T> source);
}
