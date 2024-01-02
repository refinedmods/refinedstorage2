package com.refinedmods.refinedstorage2.api.grid.operations;

import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

public class NoopGridOperations<T> implements GridOperations<T> {
    @Override
    public boolean extract(final T resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage<T> destination) {
        return false;
    }

    @Override
    public boolean insert(final T resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage<T> source) {
        return false;
    }
}
