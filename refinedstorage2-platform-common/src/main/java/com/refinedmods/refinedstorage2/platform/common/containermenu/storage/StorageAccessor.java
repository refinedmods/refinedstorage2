package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

public interface StorageAccessor {
    long getStored();

    long getCapacity();

    double getProgress();

    boolean hasCapacity();
}
