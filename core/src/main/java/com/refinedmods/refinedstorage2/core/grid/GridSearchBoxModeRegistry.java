package com.refinedmods.refinedstorage2.core.grid;

import java.util.ArrayList;
import java.util.List;

public class GridSearchBoxModeRegistry {
    private final List<GridSearchBoxMode> modes = new ArrayList<>();

    public void add(GridSearchBoxMode mode) {
        modes.add(mode);
    }

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

    public GridSearchBoxMode get(int index) {
        if (index < 0 || index >= modes.size()) {
            return getDefault();
        }
        return modes.get(index);
    }

    public int getId(GridSearchBoxMode mode) {
        int index = modes.indexOf(mode);
        if (index == -1) {
            return 0;
        }
        return index;
    }

    public GridSearchBoxMode getDefault() {
        if (modes.isEmpty()) {
            throw new IllegalStateException("No search box modes are available");
        }
        return modes.get(0);
    }
}
