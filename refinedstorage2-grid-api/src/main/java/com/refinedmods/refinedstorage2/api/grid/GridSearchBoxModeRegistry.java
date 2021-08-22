package com.refinedmods.refinedstorage2.api.grid;

public interface GridSearchBoxModeRegistry {
    GridSearchBoxModeRegistry INSTANCE = new GridSearchBoxModeRegistryImpl();

    void add(GridSearchBoxMode mode);

    GridSearchBoxMode next(GridSearchBoxMode mode);

    GridSearchBoxMode get(int index);

    int getId(GridSearchBoxMode mode);

    GridSearchBoxMode getDefault();
}
