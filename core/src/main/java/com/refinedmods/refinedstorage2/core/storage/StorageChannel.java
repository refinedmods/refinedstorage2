package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.List;
import java.util.Optional;

public interface StorageChannel<T> {
    Optional<T> extract(T template, int amount, Action action);

    Optional<T> insert(T template, int amount, Action action);

    StackList<T> getList();

    void setSources(List<Storage<T>> sources);
}
