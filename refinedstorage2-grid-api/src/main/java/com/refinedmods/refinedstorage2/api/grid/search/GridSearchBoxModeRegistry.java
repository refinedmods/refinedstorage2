package com.refinedmods.refinedstorage2.api.grid.search;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface GridSearchBoxModeRegistry {
    GridSearchBoxModeRegistry INSTANCE = new GridSearchBoxModeRegistryImpl();

    void add(GridSearchBoxMode mode);

    GridSearchBoxMode next(GridSearchBoxMode mode);

    GridSearchBoxMode get(int index);

    int getId(GridSearchBoxMode mode);

    GridSearchBoxMode getDefault();
}
