package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

public interface GridService<T> {
    void extract(T resource, GridExtractMode extractMode, InsertableStorage<T> destination);

    void insert(T resource, GridInsertMode insertMode, ExtractableStorage<T> source);
}
