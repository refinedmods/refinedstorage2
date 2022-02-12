package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

/**
 * Represents a source that can perform storage actions.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
@FunctionalInterface
public interface Source {
    /**
     * @return the name of the source
     */
    String getName();
}
