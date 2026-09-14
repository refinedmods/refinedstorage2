package com.refinedmods.refinedstorage.api.storage.tracked;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

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

    /**
     * Passes the tracked resource of every given resource to the consumer.
     * Only resources contained in the given set may be offered. The same resource may be offered more than once
     * if it is tracked by multiple underlying storages.
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

    /**
     * Collects the tracked resource of every given resource, keeping the most recently modified one.
     * This is equivalent to calling {@link #findTrackedResourceByActorType(ResourceKey, Class)} for every resource,
     * including how ties are resolved.
     *
     * @param actorType the actor type
     * @param resources the resources of interest
     * @return the newest tracked resource per resource, only containing resources from the given set
     */
    default Map<ResourceKey, TrackedResource> getTrackedResourcesByActorType(
        final Class<? extends Actor> actorType,
        final Set<ResourceKey> resources
    ) {
        final Map<ResourceKey, TrackedResource> result = HashMap.newHashMap(resources.size());
        // merge remaps as (first seen, later seen), so >= ties to the first, like findTrackedResourceByActorType
        collectTrackedResourcesByActorType(actorType, resources, (resource, candidate) -> result.merge(
            resource,
            candidate,
            (existing, other) -> existing.getTime() >= other.getTime() ? existing : other
        ));
        return result;
    }
}
