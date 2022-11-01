package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSize;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public interface Config {
    GridEntry getGrid();

    ControllerEntry getController();

    DiskDriveEntry getDiskDrive();

    SimpleEnergyUsageEntry getCable();

    StorageBlockEntry getStorageBlock();

    FluidStorageBlockEntry getFluidStorageBlock();

    SimpleEnergyUsageEntry getImporter();

    SimpleEnergyUsageEntry getExporter();

    UpgradeEntry getUpgrade();

    SimpleEnergyUsageEntry getInterface();

    SimpleEnergyUsageEntry getExternalStorage();

    interface SimpleEnergyUsageEntry {
        long getEnergyUsage();
    }

    interface GridEntry extends SimpleEnergyUsageEntry {
        boolean isLargeFont();

        int getMaxRowsStretch();

        boolean isPreventSortingWhileShiftIsDown();

        boolean isDetailedTooltip();

        boolean isRememberSearchQuery();

        boolean isSmoothScrolling();

        boolean isAutoSelected();

        void setAutoSelected(boolean autoSelected);

        Optional<ResourceLocation> getSynchronizer();

        void setSynchronizer(ResourceLocation synchronizerId);

        void clearSynchronizer();

        GridSortingDirection getSortingDirection();

        void setSortingDirection(GridSortingDirection sortingDirection);

        GridSortingType getSortingType();

        void setSortingType(GridSortingType sortingType);

        GridSize getSize();

        void setSize(GridSize size);
    }

    interface DiskDriveEntry extends SimpleEnergyUsageEntry {
        long getEnergyUsagePerDisk();
    }

    interface ControllerEntry {
        long getEnergyCapacity();
    }

    interface StorageBlockEntry {
        long get1kEnergyUsage();

        long get4kEnergyUsage();

        long get16kEnergyUsage();

        long get64kEnergyUsage();

        long getCreativeEnergyUsage();
    }

    interface FluidStorageBlockEntry {
        long get64bEnergyUsage();

        long get256bEnergyUsage();

        long get1024bEnergyUsage();

        long get4096bEnergyUsage();

        long getCreativeEnergyUsage();
    }

    interface UpgradeEntry {
        long getSpeedUpgradeEnergyUsage();

        long getStackUpgradeEnergyUsage();
    }
}
