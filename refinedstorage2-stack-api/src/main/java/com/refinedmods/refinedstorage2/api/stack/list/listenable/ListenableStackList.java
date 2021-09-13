package com.refinedmods.refinedstorage2.api.stack.list.listenable;

import com.refinedmods.refinedstorage2.api.stack.list.ProxyStackList;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;

import java.util.Optional;
import java.util.Set;

public class ListenableStackList<T> extends ProxyStackList<T> {
    private final Set<StackListListener<T>> listeners;

    public ListenableStackList(StackList<T> parent, Set<StackListListener<T>> listeners) {
        super(parent);
        this.listeners = listeners;
    }

    @Override
    public StackListResult<T> add(T resource, long amount) {
        StackListResult<T> result = super.add(resource, amount);
        listeners.forEach(listener -> listener.onChanged(result));
        return result;
    }

    @Override
    public Optional<StackListResult<T>> remove(T resource, long amount) {
        return super.remove(resource, amount)
                .map(result -> {
                    listeners.forEach(listener -> listener.onChanged(result));
                    return result;
                });
    }
}
