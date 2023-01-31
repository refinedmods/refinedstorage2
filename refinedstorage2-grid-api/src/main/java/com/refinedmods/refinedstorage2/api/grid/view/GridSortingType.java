package com.refinedmods.refinedstorage2.api.grid.view;

import java.util.Comparator;
import java.util.function.Function;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
@FunctionalInterface
public interface GridSortingType extends Function<GridView, Comparator<GridResource>> {
}
