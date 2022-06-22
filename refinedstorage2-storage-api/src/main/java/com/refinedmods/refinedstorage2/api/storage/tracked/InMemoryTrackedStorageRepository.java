package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.storage.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryTrackedStorageRepository<T> implements TrackedStorageRepository<T> {
    private final Map<Class<? extends Source>, Map<T, TrackedResource>> map = new HashMap<>();

    @Override
    public void update(T resource, Source source, long time) {
        Map<T, TrackedResource> resourceMap = map.computeIfAbsent(source.getClass(), k -> new HashMap<>());
        TrackedResource existing = resourceMap.get(resource);
        if (existing == null) {
            resourceMap.put(resource, new TrackedResource(source.getName(), time));
        } else {
            existing.update(source.getName(), time);
        }
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceBySourceType(T resource, Class<? extends Source> sourceType) {
        Map<T, TrackedResource> resourceMap = map.get(sourceType);
        if (resourceMap != null) {
            return Optional.ofNullable(resourceMap.get(resource));
        }
        return Optional.empty();
    }
}
