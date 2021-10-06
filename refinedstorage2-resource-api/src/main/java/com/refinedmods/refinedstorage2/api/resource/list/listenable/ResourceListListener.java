package com.refinedmods.refinedstorage2.api.resource.list.listenable;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;

@FunctionalInterface
public interface ResourceListListener<T> {
    void onChanged(ResourceListOperationResult<T> change);
}
