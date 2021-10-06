package com.refinedmods.refinedstorage2.api.grid.search.query;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;

import java.util.function.Predicate;

public interface GridQueryParser {
    Predicate<GridResource<?>> parse(String query) throws GridQueryParserException;
}
