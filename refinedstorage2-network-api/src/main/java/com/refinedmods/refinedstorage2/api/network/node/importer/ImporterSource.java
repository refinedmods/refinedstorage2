package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

import java.util.Iterator;

public interface ImporterSource<T> extends ExtractableStorage<T>, InsertableStorage<T> {
    Iterator<T> getResources();
}
