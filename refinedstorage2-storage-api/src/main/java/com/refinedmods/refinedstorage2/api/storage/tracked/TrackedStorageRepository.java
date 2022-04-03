package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.storage.Source;

import java.util.Optional;

public interface TrackedStorageRepository<T> {
    void update(T resource, Source source, long time);

    Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType);
}
