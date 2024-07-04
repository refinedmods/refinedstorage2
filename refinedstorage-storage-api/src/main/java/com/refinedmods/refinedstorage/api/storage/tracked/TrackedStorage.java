package com.refinedmods.refinedstorage.api.storage.tracked;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * A storage that is able to track resources being modified.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface TrackedStorage extends Storage {
    /**
     * Finds the tracked resource by actor type.
     *
     * @param resource  the resource
     * @param actorType the actor type
     * @return the tracked resource modified by the given actor type, if present
     */
    Optional<TrackedResource> findTrackedResourceByActorType(ResourceKey resource, Class<? extends Actor> actorType);
}
