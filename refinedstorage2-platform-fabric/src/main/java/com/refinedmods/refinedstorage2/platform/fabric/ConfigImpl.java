package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.platform.common.content.DefaultEnergyUsage;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.CraftingGridMatrixCloseBehavior;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSortingTypes;
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
    private GridEntryImpl grid = new GridEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private CraftingGridEntryImpl craftingGrid = new CraftingGridEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private ControllerEntryImpl controller = new ControllerEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private DiskDriveEntryImpl diskDrive = new DiskDriveEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl cable = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.CABLE);

    @ConfigEntry.Gui.CollapsibleObject
    private StorageBlockEntryImpl storageBlock = new StorageBlockEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private FluidStorageBlockEntryImpl fluidStorageBlock = new FluidStorageBlockEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl importer = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.IMPORTER);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl exporter = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.EXPORTER);

    @ConfigEntry.Gui.CollapsibleObject
    private UpgradeEntryImpl upgrade = new UpgradeEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl iface = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.INTERFACE);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl externalStorage = new SimpleEnergyUsageEntryImpl(
        DefaultEnergyUsage.EXTERNAL_STORAGE
    );

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl detector = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.DETECTOR);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl destructor = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.DESTRUCTOR);

    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl constructor = new SimpleEnergyUsageEntryImpl(DefaultEnergyUsage.CONSTRUCTOR);

    @ConfigEntry.Gui.CollapsibleObject
    private WirelessGridEntryImpl wirelessGrid = new WirelessGridEntryImpl();

    @ConfigEntry.Gui.CollapsibleObject
    private WirelessTransmitterEntryImpl wirelessTransmitter = new WirelessTransmitterEntryImpl();

    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
    }

    @Override
    public GridEntry getGrid() {
        return grid;
    }

    @Override
    public CraftingGridEntry getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public ControllerEntry getController() {
        return controller;
    }

    @Override
    public DiskDriveEntry getDiskDrive() {
        return diskDrive;
    }

    @Override
    public SimpleEnergyUsageEntry getCable() {
        return cable;
    }

    @Override
    public StorageBlockEntry getStorageBlock() {
        return storageBlock;
    }

    @Override
    public FluidStorageBlockEntry getFluidStorageBlock() {
        return fluidStorageBlock;
    }

    @Override
    public SimpleEnergyUsageEntry getImporter() {
        return importer;
    }

    @Override
    public SimpleEnergyUsageEntry getExporter() {
        return exporter;
    }

    @Override
    public UpgradeEntry getUpgrade() {
        return upgrade;
    }

    @Override
    public SimpleEnergyUsageEntry getInterface() {
        return iface;
    }

    @Override
    public SimpleEnergyUsageEntry getExternalStorage() {
        return externalStorage;
    }

    @Override
    public SimpleEnergyUsageEntry getDetector() {
        return detector;
    }

    @Override
    public SimpleEnergyUsageEntry getDestructor() {
        return destructor;
    }

    @Override
    public SimpleEnergyUsageEntry getConstructor() {
        return constructor;
    }

    @Override
    public WirelessGridEntry getWirelessGrid() {
        return wirelessGrid;
    }

    @Override
    public WirelessTransmitterEntry getWirelessTransmitter() {
        return wirelessTransmitter;
    }

    private static class GridEntryImpl implements GridEntry {
        private boolean largeFont = false;

        private long energyUsage = DefaultEnergyUsage.GRID;

        @ConfigEntry.BoundedDiscrete(min = 3L, max = 256)
        private int maxRowsStretch = 256;

        private boolean preventSortingWhileShiftIsDown = true;

        private boolean detailedTooltip = true;

        private boolean rememberSearchQuery = false;

        private boolean smoothScrolling = true;

        private boolean autoSelected = false;

        private String synchronizer = "";

        private String storageChannelType = "";

        private GridSortingDirection sortingDirection = GridSortingDirection.ASCENDING;

        private GridSortingTypes sortingType = GridSortingTypes.QUANTITY;

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
        public GridSortingTypes getSortingType() {
            return sortingType;
        }

        @Override
        public void setSortingType(final GridSortingTypes sortingType) {
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

        @Override
        public Optional<ResourceLocation> getStorageChannelType() {
            if (storageChannelType == null || storageChannelType.trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(storageChannelType).map(ResourceLocation::new);
        }

        @Override
        public void setStorageChannelType(final ResourceLocation storageChannelTypeId) {
            this.storageChannelType = storageChannelTypeId.toString();
            save();
        }

        @Override
        public void clearStorageChannelType() {
            this.storageChannelType = "";
            save();
        }

        private static void save() {
            AutoConfig.getConfigHolder(ConfigImpl.class).save();
        }
    }

    private static class CraftingGridEntryImpl implements CraftingGridEntry {
        private long energyUsage = DefaultEnergyUsage.CRAFTING_GRID;

        private CraftingGridMatrixCloseBehavior craftingMatrixCloseBehavior = CraftingGridMatrixCloseBehavior.NONE;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public CraftingGridMatrixCloseBehavior getCraftingMatrixCloseBehavior() {
            return craftingMatrixCloseBehavior;
        }
    }

    private static class DiskDriveEntryImpl implements DiskDriveEntry {
        private long energyUsage = DefaultEnergyUsage.DISK_DRIVE;

        private long energyUsagePerDisk = DefaultEnergyUsage.DISK_DRIVE_PER_DISK;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public long getEnergyUsagePerDisk() {
            return energyUsagePerDisk;
        }
    }

    private static class SimpleEnergyUsageEntryImpl implements SimpleEnergyUsageEntry {
        private long energyUsage;

        SimpleEnergyUsageEntryImpl(final long energyUsage) {
            this.energyUsage = energyUsage;
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }
    }

    private static class ControllerEntryImpl implements ControllerEntry {
        private long energyCapacity = DefaultEnergyUsage.CONTROLLER_CAPACITY;

        public long getEnergyCapacity() {
            return energyCapacity;
        }
    }

    private static class StorageBlockEntryImpl implements StorageBlockEntry {
        private long oneKEnergyUsage = DefaultEnergyUsage.ONE_K_STORAGE_BLOCK;
        private long fourKEnergyUsage = DefaultEnergyUsage.FOUR_K_STORAGE_BLOCK;
        private long sixteenKEnergyUsage = DefaultEnergyUsage.SIXTEEN_K_STORAGE_BLOCK;
        private long sixtyFourKEnergyUsage = DefaultEnergyUsage.SIXTY_FOUR_K_STORAGE_BLOCK;
        private long creativeEnergyUsage = DefaultEnergyUsage.CREATIVE_STORAGE_BLOCK;

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

    private static class FluidStorageBlockEntryImpl implements FluidStorageBlockEntry {
        private long sixtyFourBEnergyUsage = DefaultEnergyUsage.SIXTY_FOUR_B_FLUID_STORAGE_BLOCK;
        private long twoHundredFiftySixBEnergyUsage = DefaultEnergyUsage.TWO_HUNDRED_FIFTY_SIX_B_FLUID_STORAGE_BLOCK;
        private long thousandTwentyFourBEnergyUsage = DefaultEnergyUsage.THOUSAND_TWENTY_FOUR_B_FLUID_STORAGE_BLOCK;
        private long fourThousandNinetySixBEnergyUsage =
            DefaultEnergyUsage.FOUR_THOUSAND_NINETY_SIX_B_FLUID_STORAGE_BLOCK;
        private long creativeEnergyUsage = DefaultEnergyUsage.CREATIVE_FLUID_STORAGE_BLOCK;

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

    private static class UpgradeEntryImpl implements UpgradeEntry {
        private long speedUpgradeEnergyUsage = DefaultEnergyUsage.SPEED_UPGRADE;

        private long stackUpgradeEnergyUsage = DefaultEnergyUsage.STACK_UPGRADE;

        private long fortune1UpgradeEnergyUsage = DefaultEnergyUsage.FORTUNE_1_UPGRADE;

        private long fortune2UpgradeEnergyUsage = DefaultEnergyUsage.FORTUNE_2_UPGRADE;

        private long fortune3UpgradeEnergyUsage = DefaultEnergyUsage.FORTUNE_3_UPGRADE;

        private long silkTouchUpgradeEnergyUsage = DefaultEnergyUsage.SILK_TOUCH_UPGRADE;

        private long regulatorUpgradeEnergyUsage = DefaultEnergyUsage.REGULATOR_UPGRADE;

        private long rangeUpgradeEnergyUsage = DefaultEnergyUsage.RANGE_UPGRADE;

        private long creativeRangeUpgradeEnergyUsage = DefaultEnergyUsage.CREATIVE_RANGE_UPGRADE;

        private int rangeUpgradeRange = DefaultEnergyUsage.RANGE_UPGRADE_RANGE;

        @Override
        public long getSpeedUpgradeEnergyUsage() {
            return speedUpgradeEnergyUsage;
        }

        @Override
        public long getStackUpgradeEnergyUsage() {
            return stackUpgradeEnergyUsage;
        }

        @Override
        public long getFortune1UpgradeEnergyUsage() {
            return fortune1UpgradeEnergyUsage;
        }

        @Override
        public long getFortune2UpgradeEnergyUsage() {
            return fortune2UpgradeEnergyUsage;
        }

        @Override
        public long getFortune3UpgradeEnergyUsage() {
            return fortune3UpgradeEnergyUsage;
        }

        @Override
        public long getSilkTouchUpgradeEnergyUsage() {
            return silkTouchUpgradeEnergyUsage;
        }

        @Override
        public long getRegulatorUpgradeEnergyUsage() {
            return regulatorUpgradeEnergyUsage;
        }

        @Override
        public long getRangeUpgradeEnergyUsage() {
            return rangeUpgradeEnergyUsage;
        }

        @Override
        public long getCreativeRangeUpgradeEnergyUsage() {
            return creativeRangeUpgradeEnergyUsage;
        }

        @Override
        public int getRangeUpgradeRange() {
            return rangeUpgradeRange;
        }
    }

    private static class WirelessGridEntryImpl implements WirelessGridEntry {
        private long energyCapacity = DefaultEnergyUsage.WIRELESS_GRID_CAPACITY;

        private long openEnergyUsage = DefaultEnergyUsage.WIRELESS_GRID_OPEN;

        private long insertEnergyUsage = DefaultEnergyUsage.WIRELESS_GRID_INSERT;

        private long extractEnergyUsage = DefaultEnergyUsage.WIRELESS_GRID_EXTRACT;

        @Override
        public long getEnergyCapacity() {
            return energyCapacity;
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage;
        }

        @Override
        public long getInsertEnergyUsage() {
            return insertEnergyUsage;
        }

        @Override
        public long getExtractEnergyUsage() {
            return extractEnergyUsage;
        }
    }

    private static class WirelessTransmitterEntryImpl implements WirelessTransmitterEntry {
        private long energyUsage = DefaultEnergyUsage.WIRELESS_TRANSMITTER;

        private int baseRange = DefaultEnergyUsage.WIRELESS_TRANSMITTER_BASE_RANGE;

        @Override
        public long getEnergyUsage() {
            return energyUsage;
        }

        @Override
        public int getBaseRange() {
            return baseRange;
        }
    }
}
