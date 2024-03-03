package com.refinedmods.refinedstorage2.api.grid.watcher;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * A grid listener.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridWatcher {
    /**
     * Called when the activeness state of the grid has changed.
     *
     * @param newActive the new activeness state
     */
    void onActiveChanged(boolean newActive);

    /**
     * Called when a resource is changed.
     *
     * @param storageChannelType the relevant storage channel type
     * @param resource           the resource
     * @param change             the changed amount
     * @param trackedResource    the tracked resource, if present
     */
    void onChanged(
        StorageChannelType storageChannelType,
        ResourceKey resource,
        long change,
        @Nullable TrackedResource trackedResource
    );

    /**
     * Usually called when the grid network has been changed.
     */
    void invalidate();
}
