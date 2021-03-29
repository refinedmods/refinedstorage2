package com.refinedmods.refinedstorage2.core.grid.query;

import java.util.function.Predicate;

import com.refinedmods.refinedstorage2.core.grid.GridStack;

public interface GridQueryParser {
    Predicate<GridStack<?>> parse(String query) throws GridQueryParserException;
}
