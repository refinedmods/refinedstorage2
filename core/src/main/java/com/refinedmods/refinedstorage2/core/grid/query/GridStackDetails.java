package com.refinedmods.refinedstorage2.core.grid.query;

public class GridStackDetails {
    private final String name;
    private final String modId;
    private final String modName;

    public GridStackDetails(String name, String modId, String modName) {
        this.name = name;
        this.modId = modId;
        this.modName = modName;
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
}
