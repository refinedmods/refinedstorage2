package com.refinedmods.refinedstorage2.platform.common.storage;

public interface StorageAccessor {
    long getStored();

    long getCapacity();

    double getProgress();

    boolean hasCapacity();
}
