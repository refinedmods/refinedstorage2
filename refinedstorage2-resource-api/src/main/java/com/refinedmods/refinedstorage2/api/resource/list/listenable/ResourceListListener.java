package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

/**
 * A listener for resource list operations. Can be used on a {@link ListenableResourceList}.
 *
 * @param <T> the type of resource
 */
@FunctionalInterface
public interface ResourceListListener<T> {
    /**
     * Called when a list operation has occurred.
     *
     * @param change the change
     */
    void onChanged(ResourceListOperationResult<T> change);
}
