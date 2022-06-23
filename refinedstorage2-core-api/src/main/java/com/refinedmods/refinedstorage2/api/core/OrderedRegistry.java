package com.refinedmods.refinedstorage2.api.core;

import java.util.List;
import java.util.Optional;

public interface OrderedRegistry<I, T> {
    void register(I id, T value);

    boolean isEmpty();

    Optional<I> getId(T value);

    Optional<T> get(I id);

    T getDefault();

    List<T> getAll();

    T next(T value);
}
