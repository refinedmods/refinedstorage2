package com.refinedmods.refinedstorage.api.storage.external;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;

import java.util.Iterator;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface ExternalStorageProvider extends InsertableStorage, ExtractableStorage {
    Iterator<ResourceAmount> iterator();
}
