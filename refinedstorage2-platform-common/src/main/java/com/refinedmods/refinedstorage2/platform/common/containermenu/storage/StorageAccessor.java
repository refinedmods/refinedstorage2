package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceTypeAccessor;

import java.util.Set;

public interface StorageAccessor extends ResourceTypeAccessor {
    long getStored();

    long getCapacity();

    double getProgress();

    Set<StorageTooltipHelper.TooltipOption> getTooltipOptions();
}
