package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * A storage that is able to track resources being modified.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface TrackedStorage<T> extends Storage<T> {
    /**
     * Finds the tracked resource by source type.
     *
     * @param resource   the resource
     * @param sourceType the source type
     * @return the tracked resource, if present
     */
    Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType);
}
