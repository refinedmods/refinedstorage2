package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizationType;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = IdentifierUtil.MOD_ID)
public class ConfigImpl implements ConfigData, com.refinedmods.refinedstorage2.platform.abstractions.Config {
    @ConfigEntry.Gui.CollapsibleObject
    private GridImpl grid = new GridImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ControllerImpl controller = new ControllerImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private DiskDriveImpl diskDrive = new DiskDriveImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private CableImpl cable = new CableImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private StorageBlockImpl storageBlock = new StorageBlockImpl();

    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
    }

    @Override
    public Grid getGrid() {
        return grid;
    }

    @Override
    public Controller getController() {
        return controller;
    }

    @Override
    public DiskDrive getDiskDrive() {
        return diskDrive;
    }

    @Override
    public Cable getCable() {
        return cable;
    }

    @Override
    public StorageBlock getStorageBlock() {
        return storageBlock;
    }

    private static class GridImpl implements Grid {
        private boolean largeFont = false;

        private long energyUsage = 10;

        @ConfigEntry.BoundedDiscrete(min = 3L, max = 256)
        private int maxRowsStretch = 256;

        private boolean preventSortingWhileShiftIsDown = true;

        private boolean detailedTooltip = true;

        private boolean rememberSearchQuery = false;

        private boolean smoothScrolling = true;

        private boolean autoSelected = false;

        private GridSynchronizationType synchronizationType = GridSynchronizationType.OFF;

        private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;

        private GridSortingType sortingType = GridSortingType.QUANTITY;

        private GridSize size = GridSize.STRETCH;

        @Override
        public boolean isLargeFont() {
            return largeFont;
        }

        @Override
        public int getMaxRowsStretch() {
            return maxRowsStretch;
        }

        @Override
        public boolean isPreventSortingWhileShiftIsDown() {
            return preventSortingWhileShiftIsDown;
        }

        @Override
        public boolean isDetailedTooltip() {
            return detailedTooltip;
        }

        @Override
        public boolean isRememberSearchQuery() {
            return rememberSearchQuery;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public boolean isSmoothScrolling() {
            return smoothScrolling;
        }

        @Override
        public boolean isAutoSelected() {
            return autoSelected;
        }

        @Override
        public void setAutoSelected(boolean autoSelected) {
            this.autoSelected = autoSelected;
            save();
        }

        @Override
        public GridSynchronizationType getSynchronizationType() {
            return synchronizationType;
        }

        @Override
        public void setSynchronizationType(GridSynchronizationType synchronizationType) {
            this.synchronizationType = synchronizationType;
            save();
        }

        @Override
        public GridSortingDirection getSortingDirection() {
            return sortingDirection;
        }

        @Override
        public void setSortingDirection(GridSortingDirection sortingDirection) {
            this.sortingDirection = sortingDirection;
            save();
        }

        @Override
        public GridSortingType getSortingType() {
            return sortingType;
        }

        @Override
        public void setSortingType(GridSortingType sortingType) {
            this.sortingType = sortingType;
            save();
        }

        @Override
        public GridSize getSize() {
            return size;
        }

        @Override
        public void setSize(GridSize size) {
            this.size = size;
            save();
        }
    }

    private static class DiskDriveImpl implements DiskDrive {
        private long energyUsage = 10;

        private long energyUsagePerDisk = 5;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk;
        }
    }

    private static class CableImpl implements Cable {
        private long energyUsage = 0;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class ControllerImpl implements Controller {
        private long energyCapacity = 1000;

        public long getEnergyCapacity() {
            return energyCapacity;
        }
    }

    private static class StorageBlockImpl implements StorageBlock {
        private long oneKEnergyUsage = 2;
        private long fourKEnergyUsage = 4;
        private long sixteenKEnergyUsage = 6;
        private long sixtyFourKEnergyUsage = 8;
        private long creativeEnergyUsage = 16;

        @Override
        public long get1kEnergyUsage() {
            return oneKEnergyUsage;
        }

        @Override
        public long get4kEnergyUsage() {
            return fourKEnergyUsage;
        }

        @Override
        public long get16kEnergyUsage() {
            return sixteenKEnergyUsage;
        }

        @Override
        public long get64kEnergyUsage() {
            return sixtyFourKEnergyUsage;
        }

        @Override
        public long getCreativeEnergyUsage() {
            return creativeEnergyUsage;
        }
    }

    private static void save() {
        AutoConfig.getConfigHolder(ConfigImpl.class).save();
    }
}
