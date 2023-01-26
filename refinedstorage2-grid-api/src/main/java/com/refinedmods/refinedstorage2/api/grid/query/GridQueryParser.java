package com.refinedmods.refinedstorage2.api.grid.query;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;

import java.util.function.Predicate;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridQueryParser {
    Predicate<GridResource> parse(String query) throws GridQueryParserException;
}
