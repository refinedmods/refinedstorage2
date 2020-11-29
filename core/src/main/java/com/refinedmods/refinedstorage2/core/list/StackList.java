package com.refinedmods.refinedstorage2.core.list;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface StackList<T> {
    StackListResult<T> add(T template, int amount);

    Optional<StackListResult<T>> remove(T template, int amount);

    Optional<T> get(T template);

    Optional<T> get(UUID id);

    Collection<T> getAll();

    void clear();
}
