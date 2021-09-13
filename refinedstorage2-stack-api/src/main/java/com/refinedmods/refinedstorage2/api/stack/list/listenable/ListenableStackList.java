package com.refinedmods.refinedstorage2.api.stack.list.listenable;

import com.refinedmods.refinedstorage2.api.stack.list.ProxyStackList;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;

import java.util.Optional;
import java.util.Set;

public class ListenableStackList<R> extends ProxyStackList<R> {
    private final Set<StackListListener<R>> listeners;

    public ListenableStackList(StackList<R> parent, Set<StackListListener<R>> listeners) {
        super(parent);
        this.listeners = listeners;
    }

    @Override
    public StackListResult<R> add(R resource, long amount) {
        StackListResult<R> result = super.add(resource, amount);
        listeners.forEach(listener -> listener.onChanged(result));
        return result;
    }

    @Override
    public Optional<StackListResult<R>> remove(R resource, long amount) {
        return super.remove(resource, amount)
                .map(result -> {
                    listeners.forEach(listener -> listener.onChanged(result));
                    return result;
                });
    }
}
