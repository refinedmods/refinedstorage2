package com.refinedmods.refinedstorage2.api.grid;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * A grid listener.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridWatcher<T> {
    /**
     * Called when the activeness state of the grid has changed.
     *
     * @param newActive the new activeness state
     */
    void onActiveChanged(boolean newActive);

    /**
     * Called when a resource is changed.
     *
     * @param change          the change
     * @param trackedResource the tracked resource, if present
     */
    void onChanged(ResourceListOperationResult<T> change, @Nullable TrackedResource trackedResource);
}
