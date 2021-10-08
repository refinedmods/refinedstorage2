package com.refinedmods.refinedstorage2.api.storage;

public interface Storage<T> extends StorageView<T>, InsertableStorage<T>, ExtractableStorage<T> {
}
