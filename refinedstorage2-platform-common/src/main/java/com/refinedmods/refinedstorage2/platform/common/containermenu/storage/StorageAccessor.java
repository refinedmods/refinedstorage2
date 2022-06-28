package com.refinedmods.refinedstorage2.platform.common.containermenu.storage;

import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AccessModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExactModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.FilterModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.PriorityAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceTypeAccessor;

import java.util.Set;

public interface StorageAccessor extends PriorityAccessor,
        FilterModeAccessor,
        ExactModeAccessor,
        AccessModeAccessor,
        RedstoneModeAccessor,
        ResourceTypeAccessor {
    long getStored();

    long getCapacity();

    double getProgress();

    Set<StorageTooltipHelper.TooltipOption> getTooltipOptions();
}
