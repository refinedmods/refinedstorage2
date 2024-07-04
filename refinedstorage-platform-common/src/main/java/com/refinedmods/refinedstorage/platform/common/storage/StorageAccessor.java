package com.refinedmods.refinedstorage.platform.common.storage;

public interface StorageAccessor {
    long getStored();

    long getCapacity();

    double getProgress();

    boolean hasCapacity();
}
