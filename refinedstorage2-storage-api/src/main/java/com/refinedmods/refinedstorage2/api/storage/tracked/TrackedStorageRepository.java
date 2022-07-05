package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.storage.Source;

import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface TrackedStorageRepository<T> {
    void update(T resource, Source source, long time);

    Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType);
}
