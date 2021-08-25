package com.refinedmods.refinedstorage2.api.grid.search.query;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;

import java.util.function.Predicate;

public interface GridQueryParser {
    Predicate<GridStack<?>> parse(String query) throws GridQueryParserException;
}
