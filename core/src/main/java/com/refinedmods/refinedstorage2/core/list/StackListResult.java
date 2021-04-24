package com.refinedmods.refinedstorage2.core.list;

import java.util.UUID;

public class StackListResult<T> {
    private final T stack;
    private final long change;
    private final UUID id;
    private final boolean available;

    public StackListResult(T stack, long change, UUID id, boolean available) {
        this.stack = stack;
        this.change = change;
        this.id = id;
        this.available = available;
    }

    public T getStack() {
        return stack;
    }

    public long getChange() {
        return change;
    }

    public UUID getId() {
        return id;
    }

    public boolean isAvailable() {
        return available;
    }
}
