package com.refinedmods.refinedstorage2.core.storage;

import java.util.Collection;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.util.Action;

public class PrioritizedStorage<T> implements Storage<T>, Priority {
    private final int priority;
    private final Storage<T> parent;

    public PrioritizedStorage(int priority, Storage<T> parent) {
        this.priority = priority;
        this.parent = parent;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Optional<T> extract(T template, int amount, Action action) {
        return parent.extract(template, amount, action);
    }

    @Override
    public Optional<T> insert(T template, int amount, Action action) {
        return parent.insert(template, amount, action);
    }

    @Override
    public Collection<T> getStacks() {
        return parent.getStacks();
    }

    @Override
    public int getStored() {
        return parent.getStored();
    }
}
