package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class InMemoryTrackedStorageRepository implements TrackedStorageRepository {
    protected final Map<Class<? extends Actor>, Map<ResourceKey, TrackedResource>> trackedResourcesByActorType =
        new HashMap<>();

    @Override
    public void update(final ResourceKey resource, final Actor actor, final long time) {
        final Map<ResourceKey, TrackedResource> resourceMap = trackedResourcesByActorType.computeIfAbsent(
            actor.getClass(),
            k -> new HashMap<>()
        );
        final TrackedResource existing = resourceMap.get(resource);
        if (existing == null) {
            resourceMap.put(resource, new TrackedResource(actor.getName(), time));
        } else {
            existing.update(actor.getName(), time);
        }
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final ResourceKey resource,
                                                                    final Class<? extends Actor> actorType) {
        final Map<ResourceKey, TrackedResource> resources = trackedResourcesByActorType.get(actorType);
        if (resources != null) {
            return Optional.ofNullable(resources.get(resource));
        }
        return Optional.empty();
    }
}
