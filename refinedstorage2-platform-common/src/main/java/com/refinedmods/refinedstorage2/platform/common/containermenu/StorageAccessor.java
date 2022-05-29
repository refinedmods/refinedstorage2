package com.refinedmods.refinedstorage2.platform.common.containermenu;

public interface StorageAccessor extends PriorityAccessor, FilterModeAccessor, ExactModeAccessor, AccessModeAccessor, RedstoneModeAccessor, ResourceTypeAccessor {
    long getStored();

    long getCapacity();

    double getProgress();

    boolean hasCapacity();
}
