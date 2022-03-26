package com.refinedmods.refinedstorage2.api.storage.tracked;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.ProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public class TrackedStorageImpl<T> extends ProxyStorage<T> implements TrackedStorage<T> {
    private final LongSupplier clock;
    private final Map<Class<? extends Source>, Map<T, TrackedResource>> map = new HashMap<>();

    /**
     * @param parent the parent storage, may not be null
     */
    protected TrackedStorageImpl(Storage<T> parent, LongSupplier clock) {
        super(parent);
        Preconditions.checkNotNull(clock);
        this.clock = clock;
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        long inserted = super.insert(resource, amount, action, source);
        if (inserted > 0 && action == Action.EXECUTE) {
            update(resource, source);
        }
        return inserted;
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        long extracted = super.extract(resource, amount, action, source);
        if (extracted > 0 && action == Action.EXECUTE) {
            update(resource, source);
        }
        return extracted;
    }

    private void update(T resource, Source source) {
        Map<T, TrackedResource> resourceMap = map.computeIfAbsent(source.getClass(), k -> new HashMap<>());
        TrackedResource existing = resourceMap.get(resource);
        if (existing == null) {
            resourceMap.put(resource, new TrackedResource(source.getName(), clock.getAsLong()));
        } else {
            existing.update(source.getName(), clock.getAsLong());
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
