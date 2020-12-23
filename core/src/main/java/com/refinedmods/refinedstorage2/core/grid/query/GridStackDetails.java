package com.refinedmods.refinedstorage2.core.grid.query;

import java.util.Set;

public class GridStackDetails {
    private final String name;
    private final String modId;
    private final String modName;
    private final Set<String> tags;

    public GridStackDetails(String name, String modId, String modName, Set<String> tags) {
        this.name = name;
        this.modId = modId;
        this.modName = modName;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public String getModId() {
        return modId;
    }

    public String getModName() {
        return modName;
    }

    public Set<String> getTags() {
        return tags;
    }
}
