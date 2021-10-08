package com.refinedmods.refinedstorage2.api.storage.bulk;

import com.refinedmods.refinedstorage2.api.storage.Storage;

public interface BulkStorage<T> extends Storage<T> {
    long getCapacity();
}
