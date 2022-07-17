package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.common.Config;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigImpl implements Config {
    private static final String ENERGY_USAGE = "energyUsage";

    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    private final ForgeConfigSpec spec;

    private final Cable cable;
    private final Controller controller;
    private final DiskDrive diskDrive;
    private final Grid grid;
    private final StorageBlock storageBlock;
    private final FluidStorageBlock fluidStorageBlock;
    private final Importer importer;

    public ConfigImpl() {
        cable = new CableImpl();
        controller = new ControllerImpl();
        diskDrive = new DiskDriveImpl();
        grid = new GridImpl();
        storageBlock = new StorageBlockImpl();
        fluidStorageBlock = new FluidStorageBlockImpl();
        importer = new ImporterImpl();
        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
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

    private class CableImpl implements Cable {
        private final ForgeConfigSpec.LongValue energyUsage;

        private CableImpl() {
            builder.push("cable");
            energyUsage = builder.comment("The energy used by the Cable")
                .defineInRange(ENERGY_USAGE, 0, 0L, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }
    }

    private class ControllerImpl implements Controller {
        private final ForgeConfigSpec.IntValue energyCapacity;

        private ControllerImpl() {
            builder.push("controller");
            energyCapacity = builder.comment("The energy capacity of the Controller")
                .defineInRange("energyCapacity", 1000, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity.get();
        }
    }

    private class DiskDriveImpl implements DiskDrive {
        private final ForgeConfigSpec.IntValue energyUsage;
        private final ForgeConfigSpec.IntValue energyUsagePerDisk;

        private DiskDriveImpl() {
            builder.push("diskDrive");
            energyUsage = builder.comment("The energy used by the Disk Drive")
                .defineInRange(ENERGY_USAGE, 10, 0, Integer.MAX_VALUE);
            energyUsagePerDisk = builder.comment("The energy used per disk")
                .defineInRange("energyUsagePerDisk", 5, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }

        @Override
        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk.get();
        }
    }

    private class GridImpl implements Grid {
        private final ForgeConfigSpec.BooleanValue largeFont;
        private final ForgeConfigSpec.IntValue maxRowsStretch;
        private final ForgeConfigSpec.BooleanValue preventSortingWhileShiftIsDown;
        private final ForgeConfigSpec.BooleanValue detailedTooltip;
        private final ForgeConfigSpec.BooleanValue rememberSearchQuery;
        private final ForgeConfigSpec.IntValue energyUsage;
        private final ForgeConfigSpec.BooleanValue smoothScrolling;
        private final ForgeConfigSpec.BooleanValue autoSelected;
        private final ForgeConfigSpec.ConfigValue<String> synchronizer;
        private final ForgeConfigSpec.EnumValue<GridSortingDirection> sortingDirection;
        private final ForgeConfigSpec.EnumValue<GridSortingType> sortingType;
        private final ForgeConfigSpec.EnumValue<GridSize> size;

        GridImpl() {
            builder.push("grid");
            largeFont = builder
                .comment("Whether the Grid should use a large font for quantities")
                .define("largeFont", false);
            maxRowsStretch = builder
                .comment("The maximum amount of rows that can be displayed when the Grid is in stretch view mode")
                .defineInRange("maxRowsStretch", 256, 3, 256);
            preventSortingWhileShiftIsDown = builder
                .comment("Whether the Grid should avoid sorting when shift is held down")
                .define("preventSortingWhileShiftIsDown", true);
            detailedTooltip = builder
                .comment("Whether the Grid should show a detailed tooltip")
                .define("detailedTooltip", true);
            rememberSearchQuery = builder
                .comment("Whether the search query should persist when closing and re-opening the Grid")
                .define("rememberSearchQuery", false);
            energyUsage = builder
                .comment("The energy used by the Grid")
                .defineInRange(ENERGY_USAGE, 10, 0, Integer.MAX_VALUE);
            smoothScrolling = builder
                .comment("Whether the Grid should use smooth scrolling")
                .define("smoothScrolling", true);
            autoSelected = builder
                .comment("Whether the Grid search box is auto selected")
                .define("autoSelected", false);
            synchronizer = builder
                .comment("The synchronization type of the Grid search box")
                .define("synchronizer", "");
            sortingDirection = builder
                .comment("The sorting direction")
                .defineEnum("sortingDirection", GridSortingDirection.ASCENDING);
            sortingType = builder
                .comment("The sorting type")
                .defineEnum("sortingType", GridSortingType.QUANTITY);
            size = builder
                .comment("The size")
                .defineEnum("size", GridSize.STRETCH);
            builder.pop();
        }

        @Override
        public boolean isLargeFont() {
            return largeFont.get();
        }

        @Override
        public int getMaxRowsStretch() {
            return maxRowsStretch.get();
        }

        @Override
        public boolean isPreventSortingWhileShiftIsDown() {
            return preventSortingWhileShiftIsDown.get();
        }

        @Override
        public boolean isDetailedTooltip() {
            return detailedTooltip.get();
        }

        @Override
        public boolean isRememberSearchQuery() {
            return rememberSearchQuery.get();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }

        @Override
        public boolean isSmoothScrolling() {
            return smoothScrolling.get();
        }

        @Override
        public boolean isAutoSelected() {
            return autoSelected.get();
        }

        @Override
        public void setAutoSelected(final boolean autoSelected) {
            this.autoSelected.set(autoSelected);
        }

        @Override
        public Optional<ResourceLocation> getSynchronizer() {
            if (synchronizer == null || synchronizer.get().trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(synchronizer.get()).map(ResourceLocation::new);
        }

        @Override
        public void setSynchronizer(final ResourceLocation synchronizerId) {
            this.synchronizer.set(synchronizerId.toString());
        }

        @Override
        public void clearSynchronizer() {
            this.synchronizer.set("");
        }

        @Override
        public GridSortingDirection getSortingDirection() {
            return sortingDirection.get();
        }

        @Override
        public void setSortingDirection(final GridSortingDirection sortingDirection) {
            this.sortingDirection.set(sortingDirection);
        }

        @Override
        public GridSortingType getSortingType() {
            return sortingType.get();
        }

        @Override
        public void setSortingType(final GridSortingType sortingType) {
            this.sortingType.set(sortingType);
        }

        @Override
        public GridSize getSize() {
            return this.size.get();
        }

        @Override
        public void setSize(final GridSize size) {
            this.size.set(size);
        }
    }

    private class StorageBlockImpl implements StorageBlock {
        private final ForgeConfigSpec.LongValue oneKEnergyUsage;
        private final ForgeConfigSpec.LongValue fourKEnergyUsage;
        private final ForgeConfigSpec.LongValue sixteenKEnergyUsage;
        private final ForgeConfigSpec.LongValue sixtyFourKEnergyUsage;
        private final ForgeConfigSpec.LongValue creativeUsage;

        StorageBlockImpl() {
            builder.push("storageBlock");
            oneKEnergyUsage = builder
                .comment("The energy used by the 1K Storage Block")
                .defineInRange("1kEnergyUsage", 2, 0, Long.MAX_VALUE);
            fourKEnergyUsage = builder
                .comment("The energy used by the 4K Storage Block")
                .defineInRange("4kEnergyUsage", 4, 0, Long.MAX_VALUE);
            sixteenKEnergyUsage = builder
                .comment("The energy used by the 16K Storage Block")
                .defineInRange("16kEnergyUsage", 6, 0, Long.MAX_VALUE);
            sixtyFourKEnergyUsage = builder
                .comment("The energy used by the 64K Storage Block")
                .defineInRange("64kEnergyUsage", 8, 0, Long.MAX_VALUE);
            creativeUsage = builder
                .comment("The energy used by the Creative Storage Block")
                .defineInRange("creativeEnergyUsage", 16, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long get1kEnergyUsage() {
            return oneKEnergyUsage.get();
        }

        @Override
        public long get4kEnergyUsage() {
            return fourKEnergyUsage.get();
        }

        @Override
        public long get16kEnergyUsage() {
            return sixteenKEnergyUsage.get();
        }

        @Override
        public long get64kEnergyUsage() {
            return sixtyFourKEnergyUsage.get();
        }

        @Override
        public long getCreativeEnergyUsage() {
            return creativeUsage.get();
        }
    }

    private class FluidStorageBlockImpl implements FluidStorageBlock {
        private final ForgeConfigSpec.LongValue sixtyFourBEnergyUsage;
        private final ForgeConfigSpec.LongValue twoHundredFiftySixBEnergyUsage;
        private final ForgeConfigSpec.LongValue thousandTwentyFourBEnergyUsage;
        private final ForgeConfigSpec.LongValue fourThousandNinetySixBEnergyUsage;
        private final ForgeConfigSpec.LongValue creativeUsage;

        FluidStorageBlockImpl() {
            builder.push("fluidStorageBlock");
            sixtyFourBEnergyUsage = builder
                .comment("The energy used by the 64B Fluid Storage Block")
                .defineInRange("64bEnergyUsage", 2, 0, Long.MAX_VALUE);
            twoHundredFiftySixBEnergyUsage = builder
                .comment("The energy used by the 256B Fluid Storage Block")
                .defineInRange("256bEnergyUsage", 4, 0, Long.MAX_VALUE);
            thousandTwentyFourBEnergyUsage = builder
                .comment("The energy used by the 1024B Fluid Storage Block")
                .defineInRange("1024bEnergyUsage", 6, 0, Long.MAX_VALUE);
            fourThousandNinetySixBEnergyUsage = builder
                .comment("The energy used by the 4096B Fluid Storage Block")
                .defineInRange("4096bEnergyUsage", 8, 0, Long.MAX_VALUE);
            creativeUsage = builder
                .comment("The energy used by the Creative Fluid Storage Block")
                .defineInRange("creativeEnergyUsage", 16, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long get64bEnergyUsage() {
            return sixtyFourBEnergyUsage.get();
        }

        @Override
        public long get256bEnergyUsage() {
            return twoHundredFiftySixBEnergyUsage.get();
        }

        @Override
        public long get1024bEnergyUsage() {
            return thousandTwentyFourBEnergyUsage.get();
        }

        @Override
        public long get4096bEnergyUsage() {
            return fourThousandNinetySixBEnergyUsage.get();
        }

        @Override
        public long getCreativeEnergyUsage() {
            return creativeUsage.get();
        }
    }

    private class ImporterImpl implements Importer {
        private final ForgeConfigSpec.LongValue energyUsage;

        ImporterImpl() {
            builder.push("importer");
            energyUsage = builder
                .comment("The energy used by the Importer")
                .defineInRange("energyUsage", 2, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }
    }
}
