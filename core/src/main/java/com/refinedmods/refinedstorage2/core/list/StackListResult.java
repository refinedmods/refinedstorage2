package com.refinedmods.refinedstorage2.core.list;

import java.util.UUID;

public record StackListResult<T>(T stack, long change, UUID id, boolean available) {
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
