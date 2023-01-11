package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

import org.apiguardian.api.API;

/**
 * A listener for resource list operations. Can be used on a {@link ListenableResourceList}.
 *
 * @param <T> the type of resource
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface ResourceListListener<T> {
    /**
     * Called when a list operation has occurred.
     *
     * @param change the change
     */
    void onChanged(ResourceListOperationResult<T> change);
}
