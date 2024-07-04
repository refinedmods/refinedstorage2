package com.refinedmods.refinedstorage.api.storage.tracked;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface TrackedStorageRepository {
    void update(ResourceKey resource, Actor actor, long time);

    Optional<TrackedResource> findTrackedResourceByActorType(ResourceKey resource, Class<? extends Actor> actorType);
}
