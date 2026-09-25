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
     * The same resource may be passed more than once if it is tracked by multiple underlying storages.
     * Implementations of this interface might decide to override this method for performance reasons.
     *
     * @param actorType the actor type
     * @param resources the resources to look up, tracked resources outside this set are skipped
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
     * Collects the tracked resource of every given resource.
     * The result is the same as calling {@link #findTrackedResourceByActorType(ResourceKey, Class)} for every
     * resource.
     *
     * @param actorType the actor type
     * @param resources the resources of interest
     * @return the tracked resource per resource, only containing resources from the given set
     */
    default Map<ResourceKey, TrackedResource> getTrackedResourcesByActorType(
        final Class<? extends Actor> actorType,
        final Set<ResourceKey> resources
    ) {
        final Map<ResourceKey, TrackedResource> result = HashMap.newHashMap(resources.size());
        // keep the first one on equal time, matching Stream.max
        collectTrackedResourcesByActorType(actorType, resources, (resource, candidate) -> result.merge(
            resource,
            candidate,
            (existing, other) -> existing.getTime() >= other.getTime() ? existing : other
        ));
        return result;
    }
}
