package com.refinedmods.refinedstorage2.core.grid;

import java.util.Set;

public class GridStack<T> {
    private final T stack;
    private final String name;
    private final String modId;
    private final String modName;
    private final Set<String> tags;
    private boolean zeroed;

    public GridStack(T stack, String name, String modId, String modName, Set<String> tags) {
        this.stack = stack;
        this.name = name;
        this.modId = modId;
        this.modName = modName;
        this.tags = tags;
    }

    public T getStack() {
        return stack;
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

    public boolean isZeroed() {
        return zeroed;
    }

    public void setZeroed(boolean zeroed) {
        this.zeroed = zeroed;
    }
}
