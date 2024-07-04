package com.refinedmods.refinedstorage.api.storage.external;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface ExternalStorageListener {
    void beforeDetectChanges(ResourceKey resource, Actor actor);
}
