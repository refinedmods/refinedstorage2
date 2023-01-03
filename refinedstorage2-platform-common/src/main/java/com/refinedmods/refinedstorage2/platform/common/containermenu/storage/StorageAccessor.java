package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceTypeAccessor;

public interface StorageAccessor extends ResourceTypeAccessor {
    long getStored();

    long getCapacity();

    double getProgress();

    boolean showCapacityAndProgress();

    boolean showStackingInfo();
}
