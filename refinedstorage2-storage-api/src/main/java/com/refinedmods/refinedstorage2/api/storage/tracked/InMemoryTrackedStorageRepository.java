package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.storage.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class InMemoryTrackedStorageRepository<T> implements TrackedStorageRepository<T> {
    private final Map<Class<? extends Source>, Map<T, TrackedResource>> map = new HashMap<>();

    @Override
    public void update(final T resource, final Source source, final long time) {
        final Map<T, TrackedResource> resourceMap = map.computeIfAbsent(source.getClass(), k -> new HashMap<>());
        final TrackedResource existing = resourceMap.get(resource);
        if (existing == null) {
            resourceMap.put(resource, new TrackedResource(source.getName(), time));
        } else {
            existing.update(source.getName(), time);
        }
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(final T resource,
                                                                     final Class<? extends Source> sourceType) {
        final Map<T, TrackedResource> resourceMap = map.get(sourceType);
        if (resourceMap != null) {
            return Optional.ofNullable(resourceMap.get(resource));
        }
        return Optional.empty();
    }
}
