package com.refinedmods.refinedstorage2.api.storage.composite;

import org.apiguardian.api.API;

/**
 * Implement this on storages that can be prioritized.
 * The {@link CompositeStorage} will account for this.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface Priority {
    /**
     * The priority. Higher priority storages will be inserted into and extracted from first.
     *
     * @return the priority
     */
    int getPriority();
}
