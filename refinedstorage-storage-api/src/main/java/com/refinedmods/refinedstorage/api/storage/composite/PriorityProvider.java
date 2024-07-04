package com.refinedmods.refinedstorage.api.storage.composite;

import org.apiguardian.api.API;

/**
 * Implement this on {@link com.refinedmods.refinedstorage.api.storage.Storage}s that have a priority that
 * are contained in an {@link CompositeStorage}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface PriorityProvider {
    /**
     * The priority. Higher priority storages will be inserted into and extracted from first.
     *
     * @return the priority
     */
    int getPriority();
}
