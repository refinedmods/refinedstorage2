package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

/**
 * A listener for resource list operations. Can be used ona {@link ListenableResourceList}.
 *
 * @param <T> the type of resource
 */
@FunctionalInterface
public interface ResourceListListener<T> {
    /**
     * Called when a add or remove operation occurred.
     *
     * @param change the change
     */
    void onChanged(ResourceListOperationResult<T> change);
}
