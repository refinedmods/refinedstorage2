package com.refinedmods.refinedstorage.api.grid.operations;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;

public class NoopGridOperations implements GridOperations {
    @Override
    public boolean extract(final ResourceKey resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage destination) {
        return false;
    }

    @Override
    public boolean insert(final ResourceKey resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage source) {
        return false;
    }
}
