package com.refinedmods.refinedstorage2.api.grid;

import org.apiguardian.api.API;

/**
 * A grid listener.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridWatcher {
    /**
     * Called when the activeness state of the grid has changed.
     *
     * @param active the new activeness state
     */
    void onActiveChanged(boolean active);
}
