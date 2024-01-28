package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.content.DefaultEnergyUsage;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridMatrixCloseBehavior;
import com.refinedmods.refinedstorage2.platform.common.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.common.grid.GridSortingTypes;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigImpl implements Config {
    private static final String ENERGY_USAGE = "energyUsage";
    private static final String ENERGY_CAPACITY = "energyCapacity";

    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final SimpleEnergyUsageEntry cable;
    private final ControllerEntry controller;
    private final DiskDriveEntry diskDrive;
    private final GridEntry grid;
    private final CraftingGridEntry craftingGrid;
    private final StorageBlockEntry storageBlock;
    private final FluidStorageBlockEntry fluidStorageBlock;
    private final SimpleEnergyUsageEntry importer;
    private final SimpleEnergyUsageEntry exporter;
    private final UpgradeEntry upgrade;
    private final SimpleEnergyUsageEntry iface;
    private final SimpleEnergyUsageEntry externalStorage;
    private final SimpleEnergyUsageEntry detector;
    private final SimpleEnergyUsageEntry destructor;
    private final SimpleEnergyUsageEntry constructor;
    private final WirelessGridEntry wirelessGrid;
    private final WirelessTransmitterEntry wirelessTransmitter;
    private final SimpleEnergyUsageEntry storageMonitor;
    private final SimpleEnergyUsageEntry networkReceiver;
    private final SimpleEnergyUsageEntry networkTransmitter;
    private final PortableGridEntry portableGrid;

    public ConfigImpl() {
        cable = new SimpleEnergyUsageEntryImpl("cable", "Cable", DefaultEnergyUsage.CABLE);
        controller = new ControllerEntryImpl();
        diskDrive = new DiskDriveEntryImpl();
        grid = new GridEntryImpl();
        craftingGrid = new CraftingGridEntryImpl();
        storageBlock = new StorageBlockEntryImpl();
        fluidStorageBlock = new FluidStorageBlockEntryImpl();
        importer = new SimpleEnergyUsageEntryImpl("importer", "Importer", DefaultEnergyUsage.IMPORTER);
        exporter = new SimpleEnergyUsageEntryImpl("exporter", "Exporter", DefaultEnergyUsage.EXPORTER);
        upgrade = new UpgradeEntryImpl();
        iface = new SimpleEnergyUsageEntryImpl("interface", "Interface", DefaultEnergyUsage.INTERFACE);
        externalStorage = new SimpleEnergyUsageEntryImpl(
            "externalStorage",
            "External Storage",
            DefaultEnergyUsage.EXTERNAL_STORAGE
        );
        detector = new SimpleEnergyUsageEntryImpl("detector", "Detector", DefaultEnergyUsage.DETECTOR);
        destructor = new SimpleEnergyUsageEntryImpl("destructor", "Destructor", DefaultEnergyUsage.DESTRUCTOR);
        constructor = new SimpleEnergyUsageEntryImpl("constructor", "Constructor", DefaultEnergyUsage.CONSTRUCTOR);
        wirelessGrid = new WirelessGridEntryImpl();
        wirelessTransmitter = new WirelessTransmitterEntryImpl();
        storageMonitor = new SimpleEnergyUsageEntryImpl(
            "storageMonitor",
            "Storage Monitor",
            DefaultEnergyUsage.STORAGE_MONITOR
        );
        networkReceiver = new SimpleEnergyUsageEntryImpl(
            "networkReceiver",
            "Network Receiver",
            DefaultEnergyUsage.NETWORK_RECEIVER
        );
        networkTransmitter = new SimpleEnergyUsageEntryImpl(
            "networkTransmitter",
            "Network Transmitter",
            DefaultEnergyUsage.NETWORK_TRANSMITTER
        );
        portableGrid = new PortableGridEntryImpl();
        spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return spec;
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

    @Override
    public SimpleEnergyUsageEntry getStorageMonitor() {
        return storageMonitor;
    }

    @Override
    public SimpleEnergyUsageEntry getNetworkReceiver() {
        return networkReceiver;
    }

    @Override
    public SimpleEnergyUsageEntry getNetworkTransmitter() {
        return networkTransmitter;
    }

    @Override
    public PortableGridEntry getPortableGrid() {
        return portableGrid;
    }

    private class SimpleEnergyUsageEntryImpl implements SimpleEnergyUsageEntry {
        private final ModConfigSpec.LongValue energyUsage;

        SimpleEnergyUsageEntryImpl(final String path, final String readableName, final long defaultValue) {
            builder.push(path);
            energyUsage = builder
                .comment("The energy used by the " + readableName)
                .defineInRange(ENERGY_USAGE, defaultValue, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }
    }

    private class ControllerEntryImpl implements ControllerEntry {
        private final ModConfigSpec.LongValue energyCapacity;

        private ControllerEntryImpl() {
            builder.push("controller");
            energyCapacity = builder.comment("The energy capacity of the Controller")
                .defineInRange(ENERGY_CAPACITY, DefaultEnergyUsage.CONTROLLER_CAPACITY, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity.get();
        }
    }

    private class DiskDriveEntryImpl implements DiskDriveEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.LongValue energyUsagePerDisk;

        private DiskDriveEntryImpl() {
            builder.push("diskDrive");
            energyUsage = builder.comment("The energy used by the Disk Drive")
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.DISK_DRIVE, 0, Long.MAX_VALUE);
            energyUsagePerDisk = builder.comment("The energy used per disk")
                .defineInRange("energyUsagePerDisk", DefaultEnergyUsage.DISK_DRIVE_PER_DISK, 0, Long.MAX_VALUE);
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

    private class GridEntryImpl implements GridEntry {
        private final ModConfigSpec.BooleanValue largeFont;
        private final ModConfigSpec.IntValue maxRowsStretch;
        private final ModConfigSpec.BooleanValue preventSortingWhileShiftIsDown;
        private final ModConfigSpec.BooleanValue detailedTooltip;
        private final ModConfigSpec.BooleanValue rememberSearchQuery;
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.BooleanValue smoothScrolling;
        private final ModConfigSpec.BooleanValue autoSelected;
        private final ModConfigSpec.ConfigValue<String> synchronizer;
        private final ModConfigSpec.ConfigValue<String> storageChannelType;
        private final ModConfigSpec.EnumValue<GridSortingDirection> sortingDirection;
        private final ModConfigSpec.EnumValue<GridSortingTypes> sortingType;
        private final ModConfigSpec.EnumValue<GridSize> size;

        GridEntryImpl() {
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
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.GRID, 0, Long.MAX_VALUE);
            smoothScrolling = builder
                .comment("Whether the Grid should use smooth scrolling")
                .define("smoothScrolling", true);
            autoSelected = builder
                .comment("Whether the Grid search box is auto selected")
                .define("autoSelected", false);
            synchronizer = builder
                .comment("The synchronization type of the Grid search box")
                .define("synchronizer", "");
            storageChannelType = builder
                .comment("The storage channel type to be shown")
                .define("storageChannelType", "");
            sortingDirection = builder
                .comment("The sorting direction")
                .defineEnum("sortingDirection", GridSortingDirection.ASCENDING);
            sortingType = builder
                .comment("The sorting type")
                .defineEnum("sortingType", GridSortingTypes.QUANTITY);
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
        public GridSortingTypes getSortingType() {
            return sortingType.get();
        }

        @Override
        public void setSortingType(final GridSortingTypes sortingType) {
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

        @Override
        public Optional<ResourceLocation> getStorageChannelType() {
            if (storageChannelType == null || storageChannelType.get().trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(storageChannelType.get()).map(ResourceLocation::new);
        }

        @Override
        public void setStorageChannelType(final ResourceLocation storageChannelTypeId) {
            this.storageChannelType.set(storageChannelTypeId.toString());
        }

        @Override
        public void clearStorageChannelType() {
            this.storageChannelType.set("");
        }
    }

    private class CraftingGridEntryImpl implements CraftingGridEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.EnumValue<CraftingGridMatrixCloseBehavior> craftingMatrixCloseBehavior;

        CraftingGridEntryImpl() {
            builder.push("craftingGrid");
            energyUsage = builder
                .comment("The energy used by the Crafting Grid")
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.CRAFTING_GRID, 0, Long.MAX_VALUE);
            craftingMatrixCloseBehavior = builder
                .comment("What should happen to the crafting matrix slots when closing the Crafting Grid")
                .defineEnum("craftingMatrixCloseBehavior", CraftingGridMatrixCloseBehavior.NONE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }

        @Override
        public CraftingGridMatrixCloseBehavior getCraftingMatrixCloseBehavior() {
            return craftingMatrixCloseBehavior.get();
        }
    }

    private class StorageBlockEntryImpl implements StorageBlockEntry {
        private final ModConfigSpec.LongValue oneKEnergyUsage;
        private final ModConfigSpec.LongValue fourKEnergyUsage;
        private final ModConfigSpec.LongValue sixteenKEnergyUsage;
        private final ModConfigSpec.LongValue sixtyFourKEnergyUsage;
        private final ModConfigSpec.LongValue creativeUsage;

        StorageBlockEntryImpl() {
            builder.push("storageBlock");
            oneKEnergyUsage = builder
                .comment("The energy used by the 1K Storage Block")
                .defineInRange("1kEnergyUsage", DefaultEnergyUsage.ONE_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            fourKEnergyUsage = builder
                .comment("The energy used by the 4K Storage Block")
                .defineInRange("4kEnergyUsage", DefaultEnergyUsage.FOUR_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            sixteenKEnergyUsage = builder
                .comment("The energy used by the 16K Storage Block")
                .defineInRange("16kEnergyUsage", DefaultEnergyUsage.SIXTEEN_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            sixtyFourKEnergyUsage = builder
                .comment("The energy used by the 64K Storage Block")
                .defineInRange("64kEnergyUsage", DefaultEnergyUsage.SIXTY_FOUR_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            creativeUsage = builder
                .comment("The energy used by the Creative Storage Block")
                .defineInRange("creativeEnergyUsage", DefaultEnergyUsage.CREATIVE_STORAGE_BLOCK, 0, Long.MAX_VALUE);
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

    private class FluidStorageBlockEntryImpl implements FluidStorageBlockEntry {
        private final ModConfigSpec.LongValue sixtyFourBEnergyUsage;
        private final ModConfigSpec.LongValue twoHundredFiftySixBEnergyUsage;
        private final ModConfigSpec.LongValue thousandTwentyFourBEnergyUsage;
        private final ModConfigSpec.LongValue fourThousandNinetySixBEnergyUsage;
        private final ModConfigSpec.LongValue creativeUsage;

        FluidStorageBlockEntryImpl() {
            builder.push("fluidStorageBlock");
            sixtyFourBEnergyUsage = builder
                .comment("The energy used by the 64B Fluid Storage Block")
                .defineInRange(
                    "64bEnergyUsage",
                    DefaultEnergyUsage.SIXTY_FOUR_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            twoHundredFiftySixBEnergyUsage = builder
                .comment("The energy used by the 256B Fluid Storage Block")
                .defineInRange(
                    "256bEnergyUsage",
                    DefaultEnergyUsage.TWO_HUNDRED_FIFTY_SIX_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            thousandTwentyFourBEnergyUsage = builder
                .comment("The energy used by the 1024B Fluid Storage Block")
                .defineInRange(
                    "1024bEnergyUsage",
                    DefaultEnergyUsage.THOUSAND_TWENTY_FOUR_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            fourThousandNinetySixBEnergyUsage = builder
                .comment("The energy used by the 4096B Fluid Storage Block")
                .defineInRange(
                    "4096bEnergyUsage",
                    DefaultEnergyUsage.FOUR_THOUSAND_NINETY_SIX_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            creativeUsage = builder
                .comment("The energy used by the Creative Fluid Storage Block")
                .defineInRange(
                    "creativeEnergyUsage",
                    DefaultEnergyUsage.CREATIVE_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
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

    private class UpgradeEntryImpl implements UpgradeEntry {
        private final ModConfigSpec.LongValue speedUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue stackUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue fortune1UpgradeEnergyUsage;
        private final ModConfigSpec.LongValue fortune2UpgradeEnergyUsage;
        private final ModConfigSpec.LongValue fortune3UpgradeEnergyUsage;
        private final ModConfigSpec.LongValue silkTouchUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue regulatorUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue rangeUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue creativeRangeUpgradeEnergyUsage;
        private final ModConfigSpec.IntValue rangeUpgradeRange;

        UpgradeEntryImpl() {
            builder.push("upgrade");
            speedUpgradeEnergyUsage = builder
                .comment("The additional energy used per Speed Upgrade")
                .defineInRange("speedUpgradeEnergyUsage", DefaultEnergyUsage.SPEED_UPGRADE, 0, Long.MAX_VALUE);
            stackUpgradeEnergyUsage = builder
                .comment("The additional energy used by the Stack Upgrade")
                .defineInRange("stackUpgradeEnergyUsage", DefaultEnergyUsage.STACK_UPGRADE, 0, Long.MAX_VALUE);
            fortune1UpgradeEnergyUsage = builder
                .comment("The additional energy used by the Fortune 1 Upgrade")
                .defineInRange("fortune1UpgradeEnergyUsage", DefaultEnergyUsage.FORTUNE_1_UPGRADE, 0, Long.MAX_VALUE);
            fortune2UpgradeEnergyUsage = builder
                .comment("The additional energy used by the Fortune 2 Upgrade")
                .defineInRange("fortune2UpgradeEnergyUsage", DefaultEnergyUsage.FORTUNE_2_UPGRADE, 0, Long.MAX_VALUE);
            fortune3UpgradeEnergyUsage = builder
                .comment("The additional energy used by the Fortune 3 Upgrade")
                .defineInRange("fortune3UpgradeEnergyUsage", DefaultEnergyUsage.FORTUNE_3_UPGRADE, 0, Long.MAX_VALUE);
            silkTouchUpgradeEnergyUsage = builder
                .comment("The additional energy used by the Silk Touch Upgrade")
                .defineInRange("silkTouchUpgradeEnergyUsage", DefaultEnergyUsage.SILK_TOUCH_UPGRADE, 0, Long.MAX_VALUE);
            regulatorUpgradeEnergyUsage = builder
                .comment("The additional energy used by the Regulator Upgrade")
                .defineInRange("regulatorUpgradeEnergyUsage", DefaultEnergyUsage.REGULATOR_UPGRADE, 0, Long.MAX_VALUE);
            rangeUpgradeEnergyUsage = builder
                .comment("The additional energy used by the Range Upgrade")
                .defineInRange("rangeUpgradeEnergyUsage", DefaultEnergyUsage.RANGE_UPGRADE, 0, Long.MAX_VALUE);
            creativeRangeUpgradeEnergyUsage = builder
                .comment("The additional energy used by the Creative Range Upgrade")
                .defineInRange(
                    "creativeRangeUpgradeEnergyUsage",
                    DefaultEnergyUsage.CREATIVE_RANGE_UPGRADE,
                    0,
                    Long.MAX_VALUE
                );
            rangeUpgradeRange = builder
                .comment("The additional range by the Range Upgrade")
                .defineInRange("rangeUpgradeRange", DefaultEnergyUsage.RANGE_UPGRADE_RANGE, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getSpeedUpgradeEnergyUsage() {
            return speedUpgradeEnergyUsage.get();
        }

        @Override
        public long getStackUpgradeEnergyUsage() {
            return stackUpgradeEnergyUsage.get();
        }

        @Override
        public long getFortune1UpgradeEnergyUsage() {
            return fortune1UpgradeEnergyUsage.get();
        }

        @Override
        public long getFortune2UpgradeEnergyUsage() {
            return fortune2UpgradeEnergyUsage.get();
        }

        @Override
        public long getFortune3UpgradeEnergyUsage() {
            return fortune3UpgradeEnergyUsage.get();
        }

        @Override
        public long getSilkTouchUpgradeEnergyUsage() {
            return silkTouchUpgradeEnergyUsage.get();
        }

        @Override
        public long getRegulatorUpgradeEnergyUsage() {
            return regulatorUpgradeEnergyUsage.get();
        }

        @Override
        public long getRangeUpgradeEnergyUsage() {
            return rangeUpgradeEnergyUsage.get();
        }

        @Override
        public long getCreativeRangeUpgradeEnergyUsage() {
            return creativeRangeUpgradeEnergyUsage.get();
        }

        @Override
        public int getRangeUpgradeRange() {
            return rangeUpgradeRange.get();
        }
    }

    private class WirelessGridEntryImpl implements WirelessGridEntry {
        private final ModConfigSpec.LongValue energyCapacity;
        private final ModConfigSpec.LongValue openEnergyUsage;
        private final ModConfigSpec.LongValue extractEnergyUsage;
        private final ModConfigSpec.LongValue insertEnergyUsage;

        WirelessGridEntryImpl() {
            builder.push("wirelessGrid");
            energyCapacity = builder.comment("The energy capacity of the Wireless Grid")
                .defineInRange(ENERGY_CAPACITY, DefaultEnergyUsage.WIRELESS_GRID_CAPACITY, 0, Long.MAX_VALUE);
            openEnergyUsage = builder.comment("The energy used by the Wireless Grid to open")
                .defineInRange("openEnergyUsage", DefaultEnergyUsage.WIRELESS_GRID_OPEN, 0, Long.MAX_VALUE);
            extractEnergyUsage = builder.comment("The energy used by the Wireless Grid to extract resources")
                .defineInRange("extractEnergyUsage", DefaultEnergyUsage.WIRELESS_GRID_EXTRACT, 0, Long.MAX_VALUE);
            insertEnergyUsage = builder.comment("The energy used by the Wireless Grid to insert resources")
                .defineInRange("insertEnergyUsage", DefaultEnergyUsage.WIRELESS_GRID_INSERT, 0, Long.MAX_VALUE);
            builder.pop();
        }

        public long getEnergyCapacity() {
            return energyCapacity.get();
        }

        public long getOpenEnergyUsage() {
            return openEnergyUsage.get();
        }

        public long getExtractEnergyUsage() {
            return extractEnergyUsage.get();
        }

        public long getInsertEnergyUsage() {
            return insertEnergyUsage.get();
        }
    }

    private class WirelessTransmitterEntryImpl implements WirelessTransmitterEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.IntValue baseRange;

        WirelessTransmitterEntryImpl() {
            builder.push("wirelessTransmitter");

            energyUsage = builder.comment("The energy used by the Wireless Transmitter")
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.WIRELESS_TRANSMITTER, 0, Long.MAX_VALUE);
            baseRange = builder.comment("The base range of the Wireless Transmitter")
                .defineInRange("baseRange", DefaultEnergyUsage.WIRELESS_TRANSMITTER_BASE_RANGE, 0, Integer.MAX_VALUE);

            builder.pop();
        }

        public long getEnergyUsage() {
            return energyUsage.get();
        }

        public int getBaseRange() {
            return baseRange.get();
        }
    }

    private class PortableGridEntryImpl implements PortableGridEntry {
        private final ModConfigSpec.LongValue energyCapacity;
        private final ModConfigSpec.LongValue openEnergyUsage;
        private final ModConfigSpec.LongValue extractEnergyUsage;
        private final ModConfigSpec.LongValue insertEnergyUsage;

        PortableGridEntryImpl() {
            builder.push("portableGrid");
            energyCapacity = builder.comment("The energy capacity of the Portable Grid")
                .defineInRange(ENERGY_CAPACITY, DefaultEnergyUsage.PORTABLE_GRID_CAPACITY, 0, Long.MAX_VALUE);
            openEnergyUsage = builder.comment("The energy used by the Portable Grid to open")
                .defineInRange("openEnergyUsage", DefaultEnergyUsage.PORTABLE_GRID_OPEN, 0, Long.MAX_VALUE);
            extractEnergyUsage = builder.comment("The energy used by the Portable Grid to extract resources")
                .defineInRange("extractEnergyUsage", DefaultEnergyUsage.PORTABLE_GRID_EXTRACT, 0, Long.MAX_VALUE);
            insertEnergyUsage = builder.comment("The energy used by the Portable Grid to insert resources")
                .defineInRange("insertEnergyUsage", DefaultEnergyUsage.PORTABLE_GRID_INSERT, 0, Long.MAX_VALUE);
            builder.pop();
        }

        public long getEnergyCapacity() {
            return energyCapacity.get();
        }

        public long getOpenEnergyUsage() {
            return openEnergyUsage.get();
        }

        public long getExtractEnergyUsage() {
            return extractEnergyUsage.get();
        }

        public long getInsertEnergyUsage() {
            return insertEnergyUsage.get();
        }
    }
}
