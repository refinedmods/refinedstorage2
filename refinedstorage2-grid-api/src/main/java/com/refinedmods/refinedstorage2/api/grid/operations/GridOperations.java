package com.refinedmods.refinedstorage2.api.grid.operations;

import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

import org.apiguardian.api.API;

/**
 * Grid operations, used for grids to interact with the storage network.
 *
 * @param <T> the resource type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface GridOperations<T> {
    /**
     * Tries to move a resource from the network storage to the destination.
     * The amount being extracted depends on the extraction mode.
     *
     * @param resource    the resource
     * @param extractMode the extract mode
     * @param destination the destination
     */
    boolean extract(T resource, GridExtractMode extractMode, InsertableStorage<T> destination);

    /**
     * Tries to move a resource from the source to the network storage.
     * The amount being inserted depends on the insert mode.
     *
     * @param resource   the resource
     * @param insertMode the insertion mode
     * @param source     the source
     */
    boolean insert(T resource, GridInsertMode insertMode, ExtractableStorage<T> source);
}
