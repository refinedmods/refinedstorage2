package com.refinedmods.refinedstorage2.core.list.listenable;

import com.refinedmods.refinedstorage2.core.list.StackListResult;

@FunctionalInterface
public interface StackListListener<T> {
    void onChanged(StackListResult<T> change);
}
