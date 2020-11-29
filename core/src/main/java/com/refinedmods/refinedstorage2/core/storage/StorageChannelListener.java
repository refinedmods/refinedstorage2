package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.StackListResult;

@FunctionalInterface
public interface StorageChannelListener<T> {
    void onChanged(StackListResult<T> change);
}
