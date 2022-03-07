package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.Storage;

public interface CompositeStorageListener<T> {
    void onSourceAdded(Storage<T> source);

    void onSourceRemoved(Storage<T> source);
}
