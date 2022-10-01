package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;

import java.util.Optional;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.resources.ResourceLocation;

@Config(name = IdentifierUtil.MOD_ID)
public class ConfigImpl implements ConfigData, com.refinedmods.refinedstorage2.platform.common.Config {
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

    @ConfigEntry.Gui.CollapsibleObject
    private FluidStorageBlockImpl fluidStorageBlock = new FluidStorageBlockImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ImporterImpl importer = new ImporterImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ExporterImpl exporter = new ExporterImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private UpgradeImpl upgrade = new UpgradeImpl();

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

    @Override
    public FluidStorageBlock getFluidStorageBlock() {
        return fluidStorageBlock;
    }

    @Override
    public Importer getImporter() {
        return importer;
    }

    @Override
    public Exporter getExporter() {
        return exporter;
    }

    @Override
    public Upgrade getUpgrade() {
        return upgrade;
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

        private String synchronizer = "";

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
        public void setAutoSelected(final boolean autoSelected) {
            this.autoSelected = autoSelected;
            save();
        }

        @Override
        public Optional<ResourceLocation> getSynchronizer() {
            if (synchronizer == null || synchronizer.trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(synchronizer).map(ResourceLocation::new);
        }

        @Override
        public void setSynchronizer(final ResourceLocation synchronizerId) {
            this.synchronizer = synchronizerId.toString();
            save();
        }

        @Override
        public void clearSynchronizer() {
            this.synchronizer = "";
            save();
        }

        @Override
        public GridSortingDirection getSortingDirection() {
            return sortingDirection;
        }

        @Override
        public void setSortingDirection(final GridSortingDirection sortingDirection) {
            this.sortingDirection = sortingDirection;
            save();
        }

        @Override
        public GridSortingType getSortingType() {
            return sortingType;
        }

        @Override
        public void setSortingType(final GridSortingType sortingType) {
            this.sortingType = sortingType;
            save();
        }

        @Override
        public GridSize getSize() {
            return size;
        }

        @Override
        public void setSize(final GridSize size) {
            this.size = size;
            save();
        }

        private static void save() {
            AutoConfig.getConfigHolder(ConfigImpl.class).save();
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

    private static class FluidStorageBlockImpl implements FluidStorageBlock {
        private long sixtyFourBEnergyUsage = 2;
        private long twoHundredFiftySixBEnergyUsage = 4;
        private long thousandTwentyFourBEnergyUsage = 6;
        private long fourThousandNinetySixBEnergyUsage = 8;
        private long creativeEnergyUsage = 16;

        @Override
        public long get64bEnergyUsage() {
            return sixtyFourBEnergyUsage;
        }

        @Override
        public long get256bEnergyUsage() {
            return twoHundredFiftySixBEnergyUsage;
        }

        @Override
        public long get1024bEnergyUsage() {
            return thousandTwentyFourBEnergyUsage;
        }

        @Override
        public long get4096bEnergyUsage() {
            return fourThousandNinetySixBEnergyUsage;
        }

        @Override
        public long getCreativeEnergyUsage() {
            return creativeEnergyUsage;
        }
    }

    private static class ImporterImpl implements Importer {
        private long energyUsage = 2;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class ExporterImpl implements Exporter {
        private long energyUsage = 2;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class UpgradeImpl implements Upgrade {
        private long speedUpgradeEnergyUsage = 4;

        private long stackUpgradeEnergyUsage = 16;

        @Override
        public long getSpeedUpgradeEnergyUsage() {
            return speedUpgradeEnergyUsage;
        }

        @Override
        public long getStackUpgradeEnergyUsage() {
            return stackUpgradeEnergyUsage;
        }
    }
}
