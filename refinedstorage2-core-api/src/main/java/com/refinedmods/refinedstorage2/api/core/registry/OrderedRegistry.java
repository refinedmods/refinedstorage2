package com.refinedmods.refinedstorage2.api.core.registry;

import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public interface OrderedRegistry<I, T> {
    void register(I id, T value);

    boolean isEmpty();

    Optional<I> getId(T value);

    Optional<T> get(I id);

    T getDefault();

    List<T> getAll();

    T next(T value);
}
