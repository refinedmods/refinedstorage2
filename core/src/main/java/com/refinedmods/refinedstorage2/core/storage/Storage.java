package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.Action;

import java.util.Optional;
import java.util.stream.Stream;

public interface Storage<T> {
    Optional<T> extract(T template, int amount, Action action);

    Optional<T> insert(T template, int amount, Action action);

    Stream<T> getStacks();

    int getStored();
}
