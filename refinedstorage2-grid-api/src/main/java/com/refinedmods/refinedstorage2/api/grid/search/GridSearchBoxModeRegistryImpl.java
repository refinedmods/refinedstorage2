package com.refinedmods.refinedstorage2.api.grid.search;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class GridSearchBoxModeRegistryImpl implements GridSearchBoxModeRegistry {
    private final List<GridSearchBoxMode> modes = new ArrayList<>();

    @Override
    public void add(GridSearchBoxMode mode) {
        modes.add(mode);
    }

    @Override
    public GridSearchBoxMode next(GridSearchBoxMode mode) {
        int index = modes.indexOf(mode);
        int nextIndex;
        if (index == -1 || index + 1 >= modes.size()) {
            nextIndex = 0;
        } else {
            nextIndex = index + 1;
        }
        return modes.get(nextIndex);
    }

    @Override
    public GridSearchBoxMode get(int index) {
        if (index < 0 || index >= modes.size()) {
            return getDefault();
        }
        return modes.get(index);
    }

    @Override
    public int getId(GridSearchBoxMode mode) {
        int index = modes.indexOf(mode);
        if (index == -1) {
            return 0;
        }
        return index;
    }

    @Override
    public GridSearchBoxMode getDefault() {
        if (modes.isEmpty()) {
            throw new IllegalStateException("No search box modes are available");
        }
        return modes.get(0);
    }
}
