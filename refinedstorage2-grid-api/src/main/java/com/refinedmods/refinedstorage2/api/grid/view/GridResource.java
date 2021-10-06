package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Set;

public abstract class GridResource<T> {
    private final ResourceAmount<T> resourceAmount;
    private final String name;
    private final String modId;
    private final String modName;
    private final Set<String> tags;
    private boolean zeroed;

    protected GridResource(ResourceAmount<T> resourceAmount, String name, String modId, String modName, Set<String> tags) {
        this.resourceAmount = resourceAmount;
        this.name = name;
        this.modId = modId;
        this.modName = modName;
        this.tags = tags;
    }

    public ResourceAmount<T> getResourceAmount() {
        return resourceAmount;
    }

    public abstract int getId();

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
