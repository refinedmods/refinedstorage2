package com.refinedmods.refinedstorage2.core.grid.query;

import java.util.HashMap;
import java.util.Map;

public abstract class MemoizedGridStackDetailsProvider<T> implements GridStackDetailsProvider<T> {
    private final Map<T, GridStackDetails> details = new HashMap<>();

    @Override
    public GridStackDetails getDetails(T stack) {
        return details.computeIfAbsent(stack, key -> createDetails(stack));
    }

    protected abstract GridStackDetails createDetails(T stack);
}
