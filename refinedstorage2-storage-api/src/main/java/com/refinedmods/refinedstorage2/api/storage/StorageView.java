package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;

import org.apiguardian.api.API;

/**
 * Represents a storage where the contents can be retrieved.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface StorageView {
    /**
     * @return a list of resource amounts
     */
    Collection<ResourceAmount> getAll();

    /**
     * @return the amount stored
     */
    long getStored();
}
