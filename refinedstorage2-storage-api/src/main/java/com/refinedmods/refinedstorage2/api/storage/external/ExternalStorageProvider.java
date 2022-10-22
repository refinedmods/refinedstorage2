package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;

import java.util.Iterator;

public interface ExternalStorageProvider<T> extends InsertableStorage<T>, ExtractableStorage<T> {
    Iterator<ResourceAmount<T>> iterator();
}
