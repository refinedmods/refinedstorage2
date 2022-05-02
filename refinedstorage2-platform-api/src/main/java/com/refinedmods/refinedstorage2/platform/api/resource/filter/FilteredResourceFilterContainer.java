package com.refinedmods.refinedstorage2.platform.api.resource.filter;

public class FilteredResourceFilterContainer extends ResourceFilterContainer {
    private final ResourceType<?> allowedType;

    public FilteredResourceFilterContainer(int size, Runnable listener, ResourceType<?> allowedType) {
        super(size, listener);
        this.allowedType = allowedType;
    }

    @Override
    public <T> void set(int slot, ResourceType<T> type, T value) {
        if (type != this.allowedType) {
            return;
        }
        super.set(slot, type, value);
    }

    @Override
    public ResourceType<?> determineDefaultType() {
        return allowedType;
    }
}
