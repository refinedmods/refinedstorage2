package com.refinedmods.refinedstorage2.api.stack.list.listenable;

import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;

@FunctionalInterface
public interface StackListListener<T> {
    void onChanged(StackListResult<T> change);
}
