package com.refinedmods.refinedstorage2.api.storage.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class StorageTracker<T> {
    private final Supplier<Long> clock;
    private final Map<T, Entry> entries = new HashMap<>();

    public StorageTracker(Supplier<Long> clock) {
        this.clock = clock;
    }

    public void onChanged(T resource, String name) {
        entries.put(resource, new Entry(clock.get(), name));
    }

    public Optional<Entry> getEntry(T resource) {
        return Optional.ofNullable(entries.get(resource));
    }

    public record Entry(long time, String name) {
    }
}
