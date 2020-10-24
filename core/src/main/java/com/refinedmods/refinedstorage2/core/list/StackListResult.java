package com.refinedmods.refinedstorage2.core.list;

import java.util.UUID;

public class StackListResult<T> {
    private final T stack;
    private final int change;
    private final UUID id;

    public StackListResult(T stack, int change, UUID id) {
        this.stack = stack;
        this.change = change;
        this.id = id;
    }

    public T getStack() {
        return stack;
    }

    public int getChange() {
        return change;
    }

    public UUID getId() {
        return id;
    }
}
