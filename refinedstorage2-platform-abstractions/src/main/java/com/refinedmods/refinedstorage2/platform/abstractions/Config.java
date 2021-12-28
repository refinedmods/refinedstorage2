package com.refinedmods.refinedstorage2.platform.abstractions;

public interface Config {
    Grid getGrid();

    Controller getController();

    DiskDrive getDiskDrive();

    Cable getCable();

    interface Grid {
        boolean isLargeFont();

        int getMaxRowsStretch();

        boolean isPreventSortingWhileShiftIsDown();

        boolean isDetailedTooltip();

        boolean isRememberSearchQuery();

        long getEnergyUsage();

        boolean isSmoothScrolling();
    }

    interface DiskDrive {
        long getEnergyUsage();

        long getEnergyUsagePerDisk();
    }

    interface Cable {
        long getEnergyUsage();
    }

    interface Controller {
        long getCapacity();
    }
}
