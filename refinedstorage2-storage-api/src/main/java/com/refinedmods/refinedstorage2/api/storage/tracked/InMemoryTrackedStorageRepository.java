package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class InMemoryTrackedStorageRepository<T> implements TrackedStorageRepository<T> {
    protected final Map<Class<? extends Actor>, Map<T, TrackedResource>> trackedResourcesByActorType = new HashMap<>();

    @Override
    public void update(final T resource, final Actor actor, final long time) {
        final Map<T, TrackedResource> resourceMap = trackedResourcesByActorType.computeIfAbsent(
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
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        final Map<T, TrackedResource> resources = trackedResourcesByActorType.get(actorType);
        if (resources != null) {
            return Optional.ofNullable(resources.get(resource));
        }
        return Optional.empty();
    }
}
