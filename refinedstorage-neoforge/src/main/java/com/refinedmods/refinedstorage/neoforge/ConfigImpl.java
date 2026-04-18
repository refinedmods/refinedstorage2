package com.refinedmods.refinedstorage.neoforge;

import com.refinedmods.refinedstorage.api.resource.repository.SortingDirection;
import com.refinedmods.refinedstorage.common.Config;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerSearchMode;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewStyle;
import com.refinedmods.refinedstorage.common.content.DefaultEnergyUsage;
import com.refinedmods.refinedstorage.common.grid.CraftingGridMatrixCloseBehavior;
import com.refinedmods.refinedstorage.common.grid.GridSortingTypes;
import com.refinedmods.refinedstorage.common.grid.GridViewType;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSize;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationKey;

public class ConfigImpl implements Config {
    private static final String ENERGY_USAGE = "energyUsage";
    private static final String ENERGY_CAPACITY = "energyCapacity";
    private static final String OPEN_ENERGY_USAGE = "openEnergyUsage";

    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final ModConfigSpec.EnumValue<ScreenSize> screenSize;
    private final ModConfigSpec.BooleanValue requireEnergy;
    private final ModConfigSpec.BooleanValue smoothScrolling;
    private final ModConfigSpec.BooleanValue debug;
    private final ModConfigSpec.BooleanValue tenthAnniversaryCape;
    private final ModConfigSpec.IntValue maxRowsStretch;
    private final ModConfigSpec.BooleanValue searchBoxAutoSelected;
    private final ModConfigSpec.BooleanValue autocraftingNotification;
    private final ModConfigSpec.EnumValue<AutocraftingPreviewStyle> autocraftingPreviewStyle;
    private final SimpleEnergyUsageEntry cable;
    private final ControllerEntry controller;
    private final DiskDriveEntry diskDrive;
    private final DiskInterfaceEntry diskInterface;
    private final GridEntry grid;
    private final CraftingGridEntry craftingGrid;
    private final SimpleEnergyUsageEntry patternGrid;
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
    private final SimpleEnergyUsageEntry securityCard;
    private final SimpleEnergyUsageEntry fallbackSecurityCard;
    private final SimpleEnergyUsageEntry securityManager;
    private final RelayEntry relay;
    private final AutocrafterEntryImpl autocrafter;
    private final AutocrafterManagerEntryImpl autocrafterManager;
    private final SimpleEnergyUsageEntry autocraftingMonitor;
    private final WirelessAutocraftingMonitorEntryImpl wirelessAutocraftingMonitor;

    public ConfigImpl() {
        screenSize = builder
            .translation(translationKey("screenSize"))
            .defineEnum("screenSize", ScreenSize.STRETCH);
        requireEnergy = builder
            .translation(translationKey("requireEnergy"))
            .define("requireEnergy", true);
        smoothScrolling = builder
            .translation(translationKey("smoothScrolling"))
            .define("smoothScrolling", true);
        debug = builder
            .translation(translationKey("debug"))
            .define("debug", false);
        tenthAnniversaryCape = builder
            .translation(translationKey("tenthAnniversaryCape"))
            .define("tenthAnniversaryCape", false);
        maxRowsStretch = builder
            .translation(translationKey("maxRowsStretch"))
            .defineInRange("maxRowsStretch", 256, 3, 256);
        searchBoxAutoSelected = builder
            .translation(translationKey("searchBoxAutoSelected"))
            .define("searchBoxAutoSelected", false);
        autocraftingNotification = builder
            .translation(translationKey("autocraftingNotification"))
            .define("autocraftingNotification", true);
        autocraftingPreviewStyle = builder
            .translation(translationKey("autocraftingPreviewStyle"))
            .defineEnum("autocraftingPreviewStyle", AutocraftingPreviewStyle.LIST);
        cable = new SimpleEnergyUsageEntryImpl("cable", DefaultEnergyUsage.CABLE);
        controller = new ControllerEntryImpl();
        diskDrive = new DiskDriveEntryImpl();
        diskInterface = new DiskInterfaceEntryImpl();
        grid = new GridEntryImpl();
        craftingGrid = new CraftingGridEntryImpl();
        patternGrid = new SimpleEnergyUsageEntryImpl("patternGrid", DefaultEnergyUsage.PATTERN_GRID);
        storageBlock = new StorageBlockEntryImpl();
        fluidStorageBlock = new FluidStorageBlockEntryImpl();
        importer = new SimpleEnergyUsageEntryImpl("importer", DefaultEnergyUsage.IMPORTER);
        exporter = new SimpleEnergyUsageEntryImpl("exporter", DefaultEnergyUsage.EXPORTER);
        upgrade = new UpgradeEntryImpl();
        iface = new SimpleEnergyUsageEntryImpl("interface", DefaultEnergyUsage.INTERFACE);
        externalStorage = new SimpleEnergyUsageEntryImpl("externalStorage", DefaultEnergyUsage.EXTERNAL_STORAGE);
        detector = new SimpleEnergyUsageEntryImpl("detector", DefaultEnergyUsage.DETECTOR);
        destructor = new SimpleEnergyUsageEntryImpl("destructor", DefaultEnergyUsage.DESTRUCTOR);
        constructor = new SimpleEnergyUsageEntryImpl("constructor", DefaultEnergyUsage.CONSTRUCTOR);
        wirelessGrid = new WirelessGridEntryImpl();
        wirelessTransmitter = new WirelessTransmitterEntryImpl();
        storageMonitor = new SimpleEnergyUsageEntryImpl("storageMonitor", DefaultEnergyUsage.STORAGE_MONITOR);
        networkReceiver = new SimpleEnergyUsageEntryImpl("networkReceiver", DefaultEnergyUsage.NETWORK_RECEIVER);
        networkTransmitter = new SimpleEnergyUsageEntryImpl(
            "networkTransmitter",
            DefaultEnergyUsage.NETWORK_TRANSMITTER
        );
        portableGrid = new PortableGridEntryImpl();
        securityCard = new SimpleEnergyUsageEntryImpl("securityCard", DefaultEnergyUsage.SECURITY_CARD);
        fallbackSecurityCard = new SimpleEnergyUsageEntryImpl(
            "fallbackSecurityCard",
            DefaultEnergyUsage.FALLBACK_SECURITY_CARD
        );
        securityManager = new SimpleEnergyUsageEntryImpl("securityManager", DefaultEnergyUsage.SECURITY_MANAGER);
        relay = new RelayEntryImpl();
        autocrafter = new AutocrafterEntryImpl();
        autocrafterManager = new AutocrafterManagerEntryImpl();
        autocraftingMonitor = new SimpleEnergyUsageEntryImpl(
            "autocraftingMonitor",
            DefaultEnergyUsage.AUTOCRAFTING_MONITOR
        );
        wirelessAutocraftingMonitor = new WirelessAutocraftingMonitorEntryImpl();
        spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return spec;
    }

    @Override
    public ScreenSize getScreenSize() {
        return screenSize.get();
    }

    @Override
    public boolean isSmoothScrolling() {
        return smoothScrolling.get();
    }

    @Override
    public int getMaxRowsStretch() {
        return maxRowsStretch.get();
    }

    @Override
    public boolean isAutocraftingNotification() {
        return autocraftingNotification.get();
    }

    @Override
    public void setAutocraftingNotification(final boolean autocraftingNotification) {
        if (autocraftingNotification != Boolean.TRUE.equals(this.autocraftingNotification.get())) {
            this.autocraftingNotification.set(autocraftingNotification);
            ConfigImpl.this.spec.save();
        }
    }

    @Override
    public AutocraftingPreviewStyle getAutocraftingPreviewStyle() {
        return autocraftingPreviewStyle.get();
    }

    @Override
    public void setAutocraftingPreviewStyle(final AutocraftingPreviewStyle autocraftingPreviewStyle) {
        if (autocraftingPreviewStyle != this.autocraftingPreviewStyle.get()) {
            this.autocraftingPreviewStyle.set(autocraftingPreviewStyle);
            ConfigImpl.this.spec.save();
        }
    }

    @Override
    public void setScreenSize(final ScreenSize screenSize) {
        if (screenSize != this.screenSize.get()) {
            this.screenSize.set(screenSize);
            this.spec.save();
        }
    }

    @Override
    public boolean isTenthAnniversaryCape() {
        return tenthAnniversaryCape.get();
    }

    @Override
    public void setTenthAnniversaryCape(final boolean enabled) {
        if (enabled != Boolean.TRUE.equals(this.tenthAnniversaryCape.get())) {
            this.tenthAnniversaryCape.set(enabled);
            ConfigImpl.this.spec.save();
        }
    }

    @Override
    public boolean isRequireEnergy() {
        return requireEnergy.get();
    }

    @Override
    public boolean isDebug() {
        return debug.getAsBoolean();
    }

    @Override
    public boolean isSearchBoxAutoSelected() {
        return searchBoxAutoSelected.get();
    }

    @Override
    public void setSearchBoxAutoSelected(final boolean searchBoxAutoSelected) {
        if (searchBoxAutoSelected != Boolean.TRUE.equals(this.searchBoxAutoSelected.get())) {
            this.searchBoxAutoSelected.set(searchBoxAutoSelected);
            ConfigImpl.this.spec.save();
        }
    }

    @Override
    public GridEntry getGrid() {
        return grid;
    }

    @Override
    public SimpleEnergyUsageEntry getPatternGrid() {
        return patternGrid;
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
    public DiskInterfaceEntry getDiskInterface() {
        return diskInterface;
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

    @Override
    public SimpleEnergyUsageEntry getSecurityCard() {
        return securityCard;
    }

    @Override
    public SimpleEnergyUsageEntry getFallbackSecurityCard() {
        return fallbackSecurityCard;
    }

    @Override
    public SimpleEnergyUsageEntry getSecurityManager() {
        return securityManager;
    }

    @Override
    public RelayEntry getRelay() {
        return relay;
    }

    @Override
    public AutocrafterEntry getAutocrafter() {
        return autocrafter;
    }

    @Override
    public AutocrafterManagerEntry getAutocrafterManager() {
        return autocrafterManager;
    }

    @Override
    public SimpleEnergyUsageEntry getAutocraftingMonitor() {
        return autocraftingMonitor;
    }

    @Override
    public WirelessAutocraftingMonitorEntry getWirelessAutocraftingMonitor() {
        return wirelessAutocraftingMonitor;
    }

    private static String translationKey(final String value) {
        return createTranslationKey("text.autoconfig", "option." + value);
    }

    private class SimpleEnergyUsageEntryImpl implements SimpleEnergyUsageEntry {
        private final ModConfigSpec.LongValue energyUsage;

        SimpleEnergyUsageEntryImpl(final String path, final long defaultValue) {
            final String correctedPath = "interface".equals(path) ? "iface" : path;
            builder.translation(translationKey(correctedPath)).push(path);
            energyUsage = builder
                .translation(translationKey(correctedPath + "." + ENERGY_USAGE))
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
            builder.translation(translationKey("controller")).push("controller");
            energyCapacity = builder
                .translation(translationKey("controller." + ENERGY_CAPACITY))
                .defineInRange(ENERGY_CAPACITY, DefaultEnergyUsage.CONTROLLER_CAPACITY, 1, Long.MAX_VALUE);
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
            builder.translation(translationKey("diskDrive")).push("diskDrive");
            energyUsage = builder
                .translation(translationKey("diskDrive." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.DISK_DRIVE, 0, Long.MAX_VALUE);
            energyUsagePerDisk = builder
                .translation(translationKey("diskDrive.energyUsagePerDisk"))
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

    private class DiskInterfaceEntryImpl implements DiskInterfaceEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.LongValue energyUsagePerDisk;

        private DiskInterfaceEntryImpl() {
            builder.translation(translationKey("diskInterface")).push("diskInterface");
            energyUsage = builder
                .translation(translationKey("diskInterface." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.DISK_INTERFACE, 0, Long.MAX_VALUE);
            energyUsagePerDisk = builder
                .translation(translationKey("diskInterface.energyUsagePerDisk"))
                .defineInRange("energyUsagePerDisk", DefaultEnergyUsage.DISK_INTERFACE_PER_DISK, 0, Long.MAX_VALUE);
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
        private final ModConfigSpec.BooleanValue preventSortingWhileShiftIsDown;
        private final ModConfigSpec.BooleanValue detailedTooltip;
        private final ModConfigSpec.BooleanValue rememberSearchQuery;
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.ConfigValue<String> synchronizer;
        private final ModConfigSpec.ConfigValue<String> resourceType;
        private final ModConfigSpec.EnumValue<SortingDirection> sortingDirection;
        private final ModConfigSpec.EnumValue<GridSortingTypes> sortingType;
        private final ModConfigSpec.EnumValue<GridViewType> viewType;

        GridEntryImpl() {
            builder.translation(translationKey("grid")).push("grid");
            largeFont = builder
                .translation(translationKey("grid.largeFont"))
                .define("largeFont", false);
            preventSortingWhileShiftIsDown = builder
                .translation(translationKey("grid.preventSortingWhileShiftIsDown"))
                .define("preventSortingWhileShiftIsDown", true);
            detailedTooltip = builder
                .translation(translationKey("grid.detailedTooltip"))
                .define("detailedTooltip", true);
            rememberSearchQuery = builder
                .translation(translationKey("grid.rememberSearchQuery"))
                .define("rememberSearchQuery", false);
            energyUsage = builder
                .translation(translationKey("grid." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.GRID, 0, Long.MAX_VALUE);
            synchronizer = builder
                .translation(translationKey("grid.synchronizer"))
                .define("synchronizer", "");
            resourceType = builder
                .translation(translationKey("grid.resourceType"))
                .define("resourceType", "");
            sortingDirection = builder
                .translation(translationKey("grid.sortingDirection"))
                .defineEnum("sortingDirection", SortingDirection.ASCENDING);
            sortingType = builder
                .translation(translationKey("grid.sortingType"))
                .defineEnum("sortingType", GridSortingTypes.QUANTITY);
            viewType = builder
                .translation(translationKey("grid.viewType"))
                .defineEnum("viewType", GridViewType.ALL);
            builder.pop();
        }

        @Override
        public boolean isLargeFont() {
            return largeFont.get();
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
        public Optional<ResourceLocation> getSynchronizer() {
            if (synchronizer.get().trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(synchronizer.get()).map(ResourceLocation::tryParse);
        }

        @Override
        public void setSynchronizer(final ResourceLocation synchronizerId) {
            if (!synchronizerId.toString().equals(this.synchronizer.get())) {
                this.synchronizer.set(synchronizerId.toString());
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public void clearSynchronizer() {
            if (!synchronizer.get().isEmpty()) {
                this.synchronizer.set("");
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public SortingDirection getSortingDirection() {
            return sortingDirection.get();
        }

        @Override
        public void setSortingDirection(final SortingDirection sortingDirection) {
            if (sortingDirection != this.sortingDirection.get()) {
                this.sortingDirection.set(sortingDirection);
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public GridSortingTypes getSortingType() {
            return sortingType.get();
        }

        @Override
        public void setSortingType(final GridSortingTypes sortingType) {
            if (sortingType != this.sortingType.get()) {
                this.sortingType.set(sortingType);
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public GridViewType getViewType() {
            return viewType.get();
        }

        @Override
        public void setViewType(final GridViewType viewType) {
            if (viewType != this.viewType.get()) {
                this.viewType.set(viewType);
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public Optional<ResourceLocation> getResourceType() {
            if (resourceType.get().trim().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(resourceType.get()).map(ResourceLocation::tryParse);
        }

        @Override
        public void setResourceType(final ResourceLocation resourceTypeId) {
            if (!resourceTypeId.toString().equals(this.resourceType.get())) {
                this.resourceType.set(resourceTypeId.toString());
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public void clearResourceType() {
            if (!resourceType.get().isEmpty()) {
                this.resourceType.set("");
                ConfigImpl.this.spec.save();
            }
        }
    }

    private class CraftingGridEntryImpl implements CraftingGridEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.EnumValue<CraftingGridMatrixCloseBehavior> craftingMatrixCloseBehavior;

        CraftingGridEntryImpl() {
            builder.translation(translationKey("craftingGrid")).push("craftingGrid");
            energyUsage = builder
                .translation(translationKey("craftingGrid." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.CRAFTING_GRID, 0, Long.MAX_VALUE);
            craftingMatrixCloseBehavior = builder
                .translation(translationKey("craftingGrid.craftingMatrixCloseBehavior"))
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
        private final ModConfigSpec.LongValue creativeEnergyUsage;

        StorageBlockEntryImpl() {
            builder.translation(translationKey("storageBlock")).push("storageBlock");
            oneKEnergyUsage = builder
                .translation(translationKey("storageBlock.oneKEnergyUsage"))
                .defineInRange("1kEnergyUsage", DefaultEnergyUsage.ONE_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            fourKEnergyUsage = builder
                .translation(translationKey("storageBlock.fourKEnergyUsage"))
                .defineInRange("4kEnergyUsage", DefaultEnergyUsage.FOUR_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            sixteenKEnergyUsage = builder
                .translation(translationKey("storageBlock.sixteenKEnergyUsage"))
                .defineInRange("16kEnergyUsage", DefaultEnergyUsage.SIXTEEN_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            sixtyFourKEnergyUsage = builder
                .translation(translationKey("storageBlock.sixtyFourKEnergyUsage"))
                .defineInRange("64kEnergyUsage", DefaultEnergyUsage.SIXTY_FOUR_K_STORAGE_BLOCK, 0, Long.MAX_VALUE);
            creativeEnergyUsage = builder
                .translation(translationKey("storageBlock.creativeEnergyUsage"))
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
            return creativeEnergyUsage.get();
        }
    }

    private class FluidStorageBlockEntryImpl implements FluidStorageBlockEntry {
        private final ModConfigSpec.LongValue sixtyFourBEnergyUsage;
        private final ModConfigSpec.LongValue twoHundredFiftySixBEnergyUsage;
        private final ModConfigSpec.LongValue thousandTwentyFourBEnergyUsage;
        private final ModConfigSpec.LongValue fourThousandNinetySixBEnergyUsage;
        private final ModConfigSpec.LongValue creativeEnergyUsage;

        FluidStorageBlockEntryImpl() {
            builder.translation(translationKey("fluidStorageBlock")).push("fluidStorageBlock");
            sixtyFourBEnergyUsage = builder
                .translation(translationKey("fluidStorageBlock.sixtyFourBEnergyUsage"))
                .defineInRange(
                    "64bEnergyUsage",
                    DefaultEnergyUsage.SIXTY_FOUR_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            twoHundredFiftySixBEnergyUsage = builder
                .translation(translationKey("fluidStorageBlock.twoHundredFiftySixBEnergyUsage"))
                .defineInRange(
                    "256bEnergyUsage",
                    DefaultEnergyUsage.TWO_HUNDRED_FIFTY_SIX_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            thousandTwentyFourBEnergyUsage = builder
                .translation(translationKey("fluidStorageBlock.thousandTwentyFourBEnergyUsage"))
                .defineInRange(
                    "1024bEnergyUsage",
                    DefaultEnergyUsage.THOUSAND_TWENTY_FOUR_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            fourThousandNinetySixBEnergyUsage = builder
                .translation(translationKey("fluidStorageBlock.fourThousandNinetySixBEnergyUsage"))
                .defineInRange(
                    "4096bEnergyUsage",
                    DefaultEnergyUsage.FOUR_THOUSAND_NINETY_SIX_B_FLUID_STORAGE_BLOCK,
                    0,
                    Long.MAX_VALUE
                );
            creativeEnergyUsage = builder
                .translation(translationKey("fluidStorageBlock.creativeEnergyUsage"))
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
            return creativeEnergyUsage.get();
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
        private final ModConfigSpec.LongValue autocraftingUpgradeUsage;

        UpgradeEntryImpl() {
            builder.translation(translationKey("upgrade")).push("upgrade");
            speedUpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.speedUpgradeEnergyUsage"))
                .defineInRange("speedUpgradeEnergyUsage", DefaultEnergyUsage.SPEED_UPGRADE, 0, Long.MAX_VALUE);
            stackUpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.stackUpgradeEnergyUsage"))
                .defineInRange("stackUpgradeEnergyUsage", DefaultEnergyUsage.STACK_UPGRADE, 0, Long.MAX_VALUE);
            fortune1UpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.fortune1UpgradeEnergyUsage"))
                .defineInRange("fortune1UpgradeEnergyUsage", DefaultEnergyUsage.FORTUNE_1_UPGRADE, 0, Long.MAX_VALUE);
            fortune2UpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.fortune2UpgradeEnergyUsage"))
                .defineInRange("fortune2UpgradeEnergyUsage", DefaultEnergyUsage.FORTUNE_2_UPGRADE, 0, Long.MAX_VALUE);
            fortune3UpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.fortune3UpgradeEnergyUsage"))
                .defineInRange("fortune3UpgradeEnergyUsage", DefaultEnergyUsage.FORTUNE_3_UPGRADE, 0, Long.MAX_VALUE);
            silkTouchUpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.silkTouchUpgradeEnergyUsage"))
                .defineInRange("silkTouchUpgradeEnergyUsage", DefaultEnergyUsage.SILK_TOUCH_UPGRADE, 0, Long.MAX_VALUE);
            regulatorUpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.regulatorUpgradeEnergyUsage"))
                .defineInRange("regulatorUpgradeEnergyUsage", DefaultEnergyUsage.REGULATOR_UPGRADE, 0, Long.MAX_VALUE);
            rangeUpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.rangeUpgradeEnergyUsage"))
                .defineInRange("rangeUpgradeEnergyUsage", DefaultEnergyUsage.RANGE_UPGRADE, 0, Long.MAX_VALUE);
            creativeRangeUpgradeEnergyUsage = builder
                .translation(translationKey("upgrade.creativeRangeUpgradeEnergyUsage"))
                .defineInRange(
                    "creativeRangeUpgradeEnergyUsage",
                    DefaultEnergyUsage.CREATIVE_RANGE_UPGRADE,
                    0,
                    Long.MAX_VALUE
                );
            rangeUpgradeRange = builder
                .translation(translationKey("upgrade.rangeUpgradeRange"))
                .defineInRange("rangeUpgradeRange", DefaultEnergyUsage.RANGE_UPGRADE_RANGE, 0, Integer.MAX_VALUE);
            autocraftingUpgradeUsage = builder
                .translation(translationKey("upgrade.autocraftingUpgradeUsage"))
                .defineInRange("autocraftingUpgradeUsage", DefaultEnergyUsage.AUTOCRAFTING_UPGRADE, 0, Long.MAX_VALUE);
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

        @Override
        public long getAutocraftingUpgradeEnergyUsage() {
            return autocraftingUpgradeUsage.get();
        }
    }

    private class WirelessGridEntryImpl implements WirelessGridEntry {
        private final ModConfigSpec.LongValue energyCapacity;
        private final ModConfigSpec.LongValue openEnergyUsage;
        private final ModConfigSpec.LongValue extractEnergyUsage;
        private final ModConfigSpec.LongValue insertEnergyUsage;

        WirelessGridEntryImpl() {
            builder.translation(translationKey("wirelessGrid")).push("wirelessGrid");
            energyCapacity = builder
                .translation(translationKey("wirelessGrid." + ENERGY_CAPACITY))
                .defineInRange(ENERGY_CAPACITY, DefaultEnergyUsage.WIRELESS_GRID_CAPACITY, 1, Long.MAX_VALUE);
            openEnergyUsage = builder
                .translation(translationKey("wirelessGrid.openEnergyUsage"))
                .defineInRange(OPEN_ENERGY_USAGE, DefaultEnergyUsage.WIRELESS_GRID_OPEN, 0, Long.MAX_VALUE);
            extractEnergyUsage = builder
                .translation(translationKey("wirelessGrid.extractEnergyUsage"))
                .defineInRange("extractEnergyUsage", DefaultEnergyUsage.WIRELESS_GRID_EXTRACT, 0, Long.MAX_VALUE);
            insertEnergyUsage = builder
                .translation(translationKey("wirelessGrid.insertEnergyUsage"))
                .defineInRange("insertEnergyUsage", DefaultEnergyUsage.WIRELESS_GRID_INSERT, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity.get();
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage.get();
        }

        @Override
        public long getExtractEnergyUsage() {
            return extractEnergyUsage.get();
        }

        @Override
        public long getInsertEnergyUsage() {
            return insertEnergyUsage.get();
        }
    }

    private class WirelessTransmitterEntryImpl implements WirelessTransmitterEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.IntValue baseRange;

        WirelessTransmitterEntryImpl() {
            builder.translation(translationKey("wirelessTransmitter")).push("wirelessTransmitter");
            energyUsage = builder
                .translation(translationKey("wirelessTransmitter." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.WIRELESS_TRANSMITTER, 0, Long.MAX_VALUE);
            baseRange = builder
                .translation(translationKey("wirelessTransmitter.baseRange"))
                .defineInRange("baseRange", DefaultEnergyUsage.WIRELESS_TRANSMITTER_BASE_RANGE, 0, Integer.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }

        @Override
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
            builder.translation(translationKey("portableGrid")).push("portableGrid");
            energyCapacity = builder
                .translation(translationKey("portableGrid." + ENERGY_CAPACITY))
                .defineInRange(ENERGY_CAPACITY, DefaultEnergyUsage.PORTABLE_GRID_CAPACITY, 1, Long.MAX_VALUE);
            openEnergyUsage = builder
                .translation(translationKey("portableGrid.openEnergyUsage"))
                .defineInRange(OPEN_ENERGY_USAGE, DefaultEnergyUsage.PORTABLE_GRID_OPEN, 0, Long.MAX_VALUE);
            extractEnergyUsage = builder
                .translation(translationKey("portableGrid.extractEnergyUsage"))
                .defineInRange("extractEnergyUsage", DefaultEnergyUsage.PORTABLE_GRID_EXTRACT, 0, Long.MAX_VALUE);
            insertEnergyUsage = builder
                .translation(translationKey("portableGrid.insertEnergyUsage"))
                .defineInRange("insertEnergyUsage", DefaultEnergyUsage.PORTABLE_GRID_INSERT, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity.get();
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage.get();
        }

        @Override
        public long getExtractEnergyUsage() {
            return extractEnergyUsage.get();
        }

        @Override
        public long getInsertEnergyUsage() {
            return insertEnergyUsage.get();
        }
    }

    private class RelayEntryImpl implements RelayEntry {
        private final ModConfigSpec.LongValue inputNetworkEnergyUsage;
        private final ModConfigSpec.LongValue outputNetworkEnergyUsage;

        RelayEntryImpl() {
            builder.translation(translationKey("relay")).push("relay");
            inputNetworkEnergyUsage = builder
                .translation(translationKey("relay.inputNetworkEnergyUsage"))
                .defineInRange("inputNetworkEnergyUsage", DefaultEnergyUsage.RELAY_INPUT_NETWORK, 0, Long.MAX_VALUE);
            outputNetworkEnergyUsage = builder
                .translation(translationKey("relay.outputNetworkEnergyUsage"))
                .defineInRange("outputNetworkEnergyUsage", DefaultEnergyUsage.RELAY_OUTPUT_NETWORK, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getInputNetworkEnergyUsage() {
            return inputNetworkEnergyUsage.get();
        }

        @Override
        public long getOutputNetworkEnergyUsage() {
            return outputNetworkEnergyUsage.get();
        }
    }

    private class AutocrafterEntryImpl implements AutocrafterEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.LongValue energyUsagePerPattern;

        AutocrafterEntryImpl() {
            builder.translation(translationKey("autocrafter")).push("autocrafter");
            energyUsage = builder
                .translation(translationKey("autocrafter." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.AUTOCRAFTER, 0, Long.MAX_VALUE);
            energyUsagePerPattern = builder
                .translation(translationKey("autocrafter.energyUsagePerPattern"))
                .defineInRange("energyUsagePerPattern", DefaultEnergyUsage.AUTOCRAFTER_PER_PATTERN, 0, Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyUsagePerPattern() {
            return energyUsagePerPattern.get();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }
    }

    private class AutocrafterManagerEntryImpl implements AutocrafterManagerEntry {
        private final ModConfigSpec.LongValue energyUsage;
        private final ModConfigSpec.EnumValue<AutocrafterManagerSearchMode> searchMode;
        private final ModConfigSpec.EnumValue<AutocrafterManagerViewType> viewType;

        AutocrafterManagerEntryImpl() {
            builder.translation(translationKey("autocrafterManager")).push("autocrafterManager");
            energyUsage = builder
                .translation(translationKey("autocrafterManager." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, DefaultEnergyUsage.AUTOCRAFTER_MANAGER, 0, Long.MAX_VALUE);
            searchMode = builder
                .translation(translationKey("autocrafterManager.searchMode"))
                .defineEnum("searchMode", AutocrafterManagerSearchMode.ALL);
            viewType = builder
                .translation(translationKey("autocrafterManager.viewType"))
                .defineEnum("viewType", AutocrafterManagerViewType.VISIBLE);
            builder.pop();
        }

        @Override
        public void setSearchMode(final AutocrafterManagerSearchMode searchMode) {
            if (searchMode != this.searchMode.get()) {
                this.searchMode.set(searchMode);
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public AutocrafterManagerSearchMode getSearchMode() {
            return searchMode.get();
        }

        @Override
        public void setViewType(final AutocrafterManagerViewType viewType) {
            if (viewType != this.viewType.get()) {
                this.viewType.set(viewType);
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public AutocrafterManagerViewType getViewType() {
            return viewType.get();
        }

        @Override
        public long getEnergyUsage() {
            return energyUsage.get();
        }
    }

    private class WirelessAutocraftingMonitorEntryImpl implements WirelessAutocraftingMonitorEntry {
        private final ModConfigSpec.LongValue energyCapacity;
        private final ModConfigSpec.LongValue openEnergyUsage;
        private final ModConfigSpec.LongValue cancelEnergyUsage;
        private final ModConfigSpec.LongValue cancelAllEnergyUsage;

        WirelessAutocraftingMonitorEntryImpl() {
            builder.translation(translationKey("wirelessAutocraftingMonitor")).push("wirelessAutocraftingMonitor");
            energyCapacity = builder
                .translation(translationKey("wirelessAutocraftingMonitor." + ENERGY_CAPACITY))
                .defineInRange(ENERGY_CAPACITY, DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_CAPACITY, 1,
                    Long.MAX_VALUE);
            openEnergyUsage = builder
                .translation(translationKey("wirelessAutocraftingMonitor.openEnergyUsage"))
                .defineInRange(OPEN_ENERGY_USAGE, DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_OPEN, 0,
                    Long.MAX_VALUE);
            cancelEnergyUsage = builder
                .translation(translationKey("wirelessAutocraftingMonitor.cancelEnergyUsage"))
                .defineInRange("cancelEnergyUsage", DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_CANCEL, 0,
                    Long.MAX_VALUE);
            cancelAllEnergyUsage = builder
                .translation(translationKey("wirelessAutocraftingMonitor.cancelAllEnergyUsage"))
                .defineInRange("cancelAllEnergyUsage", DefaultEnergyUsage.WIRELESS_AUTOCRAFTING_MONITOR_CANCEL_ALL, 0,
                    Long.MAX_VALUE);
            builder.pop();
        }

        @Override
        public long getEnergyCapacity() {
            return energyCapacity.get();
        }

        @Override
        public long getOpenEnergyUsage() {
            return openEnergyUsage.get();
        }

        @Override
        public long getCancelEnergyUsage() {
            return cancelEnergyUsage.get();
        }

        @Override
        public long getCancelAllEnergyUsage() {
            return cancelAllEnergyUsage.get();
        }
    }
}
