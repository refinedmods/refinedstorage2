package com.refinedmods.refinedstorage2.core.storage;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.list.StackListListener;

public interface StorageChannel<T> extends Storage<T> {
    void addListener(StackListListener<T> listener);

    void removeListener(StackListListener<T> listener);

    Optional<T> extract(T template, int amount, Source source);

    Optional<T> insert(T template, int amount, Source source);

    StorageTracker<T, ?> getTracker();

    Optional<T> get(T template);

    void sortSources();
}
