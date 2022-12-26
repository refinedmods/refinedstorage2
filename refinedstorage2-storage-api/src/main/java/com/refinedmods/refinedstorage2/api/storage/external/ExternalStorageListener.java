package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.storage.Actor;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface ExternalStorageListener<T> {
    void beforeDetectChanges(T resource, Actor actor);
}
