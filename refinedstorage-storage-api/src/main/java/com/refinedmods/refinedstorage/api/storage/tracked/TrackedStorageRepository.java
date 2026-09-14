package com.refinedmods.refinedstorage.api.storage.tracked;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface TrackedStorageRepository {
    void update(ResourceKey resource, Actor actor, long time);

    Optional<TrackedResource> findTrackedResourceByActorType(ResourceKey resource, Class<? extends Actor> actorType);

    /**
     * Passes the tracked resource of every given resource to the consumer.
     * Only resources contained in the given set may be offered.
     * Overriding the default implementation is an optimization, never a correctness requirement.
     *
     * @param actorType the actor type
     * @param resources the resources of interest
     * @param consumer  the consumer, receiving the resource and its tracked resource
     */
    default void collectTrackedResourcesByActorType(final Class<? extends Actor> actorType,
                                                    final Set<ResourceKey> resources,
                                                    final BiConsumer<ResourceKey, TrackedResource> consumer) {
        for (final ResourceKey resource : resources) {
            findTrackedResourceByActorType(resource, actorType)
                .ifPresent(trackedResource -> consumer.accept(resource, trackedResource));
        }
    }
}
