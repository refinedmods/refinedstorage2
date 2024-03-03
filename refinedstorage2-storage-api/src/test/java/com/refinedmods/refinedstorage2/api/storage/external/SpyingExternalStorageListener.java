package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.ArrayList;
import java.util.List;

class SpyingExternalStorageListener implements ExternalStorageListener {
    public final List<ResourceKey> resources = new ArrayList<>();
    public final List<Actor> actors = new ArrayList<>();

    @Override
    public void beforeDetectChanges(final ResourceKey resource, final Actor actor) {
        resources.add(resource);
        actors.add(actor);
    }
}
