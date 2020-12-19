package com.refinedmods.refinedstorage2.core.list;

@FunctionalInterface
public interface StackListListener<T> {
    void onChanged(StackListResult<T> change);
}
