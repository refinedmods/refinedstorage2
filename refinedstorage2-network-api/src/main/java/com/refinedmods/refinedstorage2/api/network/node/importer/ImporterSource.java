package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;

import java.util.Iterator;

public interface ImporterSource<T> extends ExtractableStorage<T> {
    Iterator<T> getResources();
}
