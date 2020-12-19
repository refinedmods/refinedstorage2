package com.refinedmods.refinedstorage2.core.list;


import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ListenableStackList<T> implements StackList<T> {
    private final StackList<T> parent;
    private final Set<StackListListener<T>> listeners;

    public ListenableStackList(StackList<T> parent, Set<StackListListener<T>> listeners) {
        this.parent = parent;
        this.listeners = listeners;
    }

    @Override
    public StackListResult<T> add(T template, int amount) {
        StackListResult<T> result = parent.add(template, amount);
        listeners.forEach(listener -> listener.onChanged(result));
        return result;
    }

    @Override
    public Optional<StackListResult<T>> remove(T template, int amount) {
        return parent.remove(template, amount)
            .map(result -> {
                listeners.forEach(listener -> listener.onChanged(result));
                return result;
            });
    }

    @Override
    public Optional<T> get(T template) {
        return parent.get(template);
    }

    @Override
    public Optional<T> get(UUID id) {
        return parent.get(id);
    }

    @Override
    public Collection<T> getAll() {
        return parent.getAll();
    }

    @Override
    public void clear() {
        parent.clear();
    }
}
