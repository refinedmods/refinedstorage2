package com.refinedmods.refinedstorage2.platform.abstractions;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizationType;

public interface Config {
    Grid getGrid();

    Controller getController();

    DiskDrive getDiskDrive();

    Cable getCable();

    StorageBlock getStorageBlock();

    interface Grid {
        boolean isLargeFont();

        int getMaxRowsStretch();

        boolean isPreventSortingWhileShiftIsDown();

        boolean isDetailedTooltip();

        boolean isRememberSearchQuery();

        long getEnergyUsage();

        boolean isSmoothScrolling();

        boolean isAutoSelected();

        void setAutoSelected(boolean autoSelected);

        GridSynchronizationType getSynchronizationType();

        void setSynchronizationType(GridSynchronizationType synchronizationType);

        GridSortingDirection getSortingDirection();

        void setSortingDirection(GridSortingDirection sortingDirection);

        GridSortingType getSortingType();

        void setSortingType(GridSortingType sortingType);

        GridSize getSize();

        void setSize(GridSize size);
    }

    interface DiskDrive {
        long getEnergyUsage();

        long getEnergyUsagePerDisk();
    }

    interface Cable {
        long getEnergyUsage();
    }

    interface Controller {
        long getEnergyCapacity();
    }

    interface StorageBlock {
        long get1kEnergyUsage();

        long get4kEnergyUsage();

        long get16kEnergyUsage();

        long get64kEnergyUsage();

        long getCreativeEnergyUsage();
    }
}
