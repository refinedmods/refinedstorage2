package com.refinedmods.refinedstorage2.core.grid.query;

public interface GridStackDetailsProvider<T> {
    GridStackDetails getDetails(T stack);
}
